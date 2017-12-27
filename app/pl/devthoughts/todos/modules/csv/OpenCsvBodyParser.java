package pl.devthoughts.todos.modules.csv;

import akka.util.ByteString;
import com.google.inject.Inject;
import com.opencsv.bean.CsvToBeanBuilder;
import io.vavr.control.Either;
import io.vavr.control.Option;
import pl.devthoughts.todos.controllers.TodoItemRequest;
import play.api.http.ParserConfiguration;
import play.http.HttpErrorHandler;
import play.mvc.BodyParser;
import play.mvc.Http;

import java.io.StringReader;
import java.util.List;

public class OpenCsvBodyParser extends BodyParser.BufferingBodyParser<List<TodoItemRequest>> {

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
            .flatMap(ct -> hasBody(request))
            .map(withBody -> doParse(bytes))
            .getOrElseThrow(errMessage -> {
                throw new IllegalArgumentException(errMessage);
            });
    }

    private Either<String, String> hasCsvMimeType(Http.RequestHeader request) {
        return Option.ofOptional(request.contentType())
            .filter(TEXT_CSV_MIME_TYPE::equalsIgnoreCase)
            .toEither(NON_CSV_MIME_TYPE_ERR_MSG);
    }

    private Either<String, Boolean> hasBody(Http.RequestHeader request) {
        return request.hasBody() ? Either.right(true) : Either.left(NO_BODY_ERR_MSG);
    }

    @SuppressWarnings("unchecked")
    private List<TodoItemRequest> doParse(ByteString bytes) {
        return new CsvToBeanBuilder(new StringReader(bytes.utf8String()))
            .withType(TodoItemRequest.class)
            .withThrowExceptions(true)
            .build()
            .parse();
    }

}
