package pl.devthoughts.todos.modules;

import akka.util.ByteString;

import com.google.inject.Inject;
import com.opencsv.bean.CsvToBeanBuilder;

import pl.devthoughts.todos.controllers.TodoItemRequest;
import pl.devthoughts.todos.domain.TodoItem;

import java.io.StringReader;
import java.util.List;

import play.api.http.ParserConfiguration;
import play.http.HttpErrorHandler;
import play.mvc.BodyParser;
import play.mvc.Http;

public class OpenCsvBodyParser extends BodyParser.BufferingBodyParser<List<TodoItem>> {

    @Inject
    protected OpenCsvBodyParser(ParserConfiguration config, HttpErrorHandler errorHandler) {
        super(config.maxMemoryBuffer(), errorHandler, "Error decoding csv body");
    }

    @Override
    protected List<TodoItem> parse(Http.RequestHeader request, ByteString bytes)
        throws Exception {
        request.contentType()
            .filter(ct -> "text/csv".equalsIgnoreCase(ct))
            .orElseThrow(() -> new IllegalArgumentException("Expected text/csv content type."));

        if (request.hasBody()) {
            final String body = bytes.utf8String();
            return (List<TodoItem>) new CsvToBeanBuilder(new StringReader(body)).withType(TodoItemRequest.class).build()
                .parse();
        } else {
            throw new IllegalStateException("Request has no body");
        }
    }

}
