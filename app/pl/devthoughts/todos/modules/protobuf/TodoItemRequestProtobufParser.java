package pl.devthoughts.todos.modules.protobuf;

import akka.util.ByteString;

import com.google.inject.Inject;

import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;

import pl.devthoughts.todos.controllers.TodoItemRequest;
import pl.devthougths.todos.ProtobufTodoItem;

import play.api.http.ParserConfiguration;
import play.http.HttpErrorHandler;
import play.mvc.BodyParser;
import play.mvc.Http;

import static pl.devthoughts.todos.TimeUtils.asLocalDateTime;

public class TodoItemRequestProtobufParser extends BodyParser.BufferingBodyParser<TodoItemRequest> {

    private static final String ERR_MSG_PREFIX = "Error decoding protobuf body";

    static final String NON_PROTOBUF_MIME_TYPE_ERR_MSG = "Expected application/x-protobuf content type.";
    static final String NO_BODY_ERR_MSG = "Request has no body";

    public static final String PROTOBUF_MIME_TYPE = "application/x-protobuf";

    @Inject
    protected TodoItemRequestProtobufParser(ParserConfiguration config, HttpErrorHandler errorHandler) {
        super(config.maxMemoryBuffer(), errorHandler, ERR_MSG_PREFIX);
    }

    @Override
    protected TodoItemRequest parse(Http.RequestHeader request, ByteString bytes)
        throws Exception {
        return hasProtobufMimeType(request)
            .flatMap(ct -> hasBody(request))
            .flatMap(withBody -> doParse(bytes))
            .getOrElseThrow(errMessage -> {
                throw new IllegalArgumentException(errMessage);
            });
    }

    private Either<String, String> hasProtobufMimeType(Http.RequestHeader request) {
        return Option.ofOptional(request.contentType())
            .filter(PROTOBUF_MIME_TYPE::equalsIgnoreCase)
            .toEither(NON_PROTOBUF_MIME_TYPE_ERR_MSG);
    }

    private Either<String, Boolean> hasBody(Http.RequestHeader request) {
        return request.hasBody() ? Either.right(true) : Either.left(NO_BODY_ERR_MSG);
    }

    private Either<String, TodoItemRequest> doParse(ByteString bytes) {
        return Try.of(() -> ProtobufTodoItem.CreateItemRequest.parseFrom(bytes.toArray()))
            .map(req -> new TodoItemRequest(req.getName(), asLocalDateTime(req.getDueDate())))
            .toEither()
            .mapLeft(Throwable::getMessage);
    }

}
