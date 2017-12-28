package pl.devthoughts.todos.modules.csv;

import akka.util.ByteString;
import com.google.inject.Inject;
import com.opencsv.bean.CsvToBeanBuilder;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.jetbrains.annotations.NotNull;
import org.slf4j.helpers.BasicMarkerFactory;
import pl.devthoughts.todos.controllers.TodoItemRequest;
import play.api.DefaultMarkerContext;
import play.api.Logger;
import play.api.http.ParserConfiguration;
import play.http.HttpErrorHandler;
import play.mvc.BodyParser;
import play.mvc.Http;

import java.io.StringReader;
import java.util.List;

import static org.slf4j.Marker.ANY_MARKER;

public class OpenCsvBodyParser extends BodyParser.BufferingBodyParser<List<TodoItemRequest>> {

    private static final Logger LOG = Logger.apply(OpenCsvBodyParser.class);

    private static final String ERR_MSG_PREFIX = "Error decoding CSV body";

    static final String NON_CSV_MIME_TYPE_ERR_MSG = "Expected text/csv content type.";
    static final String NO_BODY_ERR_MSG = "Request has no body";
    static final String TEXT_CSV_MIME_TYPE = "text/csv";

    @Inject
    protected OpenCsvBodyParser(ParserConfiguration config, HttpErrorHandler errorHandler) {
        super(config.maxMemoryBuffer(), errorHandler, ERR_MSG_PREFIX);
    }

    @Override
    protected List<TodoItemRequest> parse(Http.RequestHeader request, ByteString bytes)
        throws Exception {
        return hasCsvMimeType(request)
            .mapTry(ct -> hasBody(request))
            .mapTry(withBody -> doParse(bytes))
            .getOrElseThrow(err -> new IllegalArgumentException(err.getMessage()));
    }

    private Try<String> hasCsvMimeType(Http.RequestHeader request) {
        return Option.ofOptional(request.contentType())
            .filter(TEXT_CSV_MIME_TYPE::equalsIgnoreCase)
            .toTry(() -> new IllegalArgumentException(NON_CSV_MIME_TYPE_ERR_MSG));
    }

    private boolean hasBody(Http.RequestHeader request) {
        if (request.hasBody()) {
            return true;
        } else {
            throw new IllegalArgumentException(NO_BODY_ERR_MSG);
        }
    }

    @SuppressWarnings("unchecked")
    private List<TodoItemRequest> doParse(ByteString bytes) {
        return Try.of(() -> new CsvToBeanBuilder(new StringReader(bytes.utf8String()))
            .withType(TodoItemRequest.class)
            .withThrowExceptions(true)
            .build()
            .parse())
            .getOrElseThrow(err -> {
                LOG.error(() -> "Cannot parse request: " + err, markerContext());
                return new IllegalArgumentException(err.getMessage(), err);
            });
    }

    @NotNull
    private DefaultMarkerContext markerContext() {
        return new DefaultMarkerContext(new BasicMarkerFactory().getMarker(ANY_MARKER));
    }
}
