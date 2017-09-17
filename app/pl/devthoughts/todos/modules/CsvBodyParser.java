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

public class CsvBodyParser extends BodyParser.BufferingBodyParser<List<TodoItem>> {

    @Inject
    protected CsvBodyParser(ParserConfiguration config, HttpErrorHandler errorHandler) {
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
            final List<TodoItem> todos =
                new CsvToBeanBuilder(new StringReader(body)).withType(TodoItemRequest.class).build()
                    .parse();

            // a dedicated CSV endpoint in the app

            // tests for it

            // how to get already parsed data in a form of entities

            // check for a content of headers line ???

            // check for a format of data

            // expected header
            // name,dueDate

            // data
            // Write an essay,2017-12-12
            // Replace a bulb,2017-09-09
            // Start a course,2017-10-10

            return todos;
        } else {
            throw new IllegalStateException("Request has no body");
        }
    }

}
