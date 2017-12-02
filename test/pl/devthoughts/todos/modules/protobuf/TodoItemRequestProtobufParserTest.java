package pl.devthoughts.todos.modules.protobuf;

import akka.util.ByteString;

import com.google.protobuf.Timestamp;

import io.vavr.collection.List;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import pl.devthoughts.todos.controllers.TodoItemRequest;
import pl.devthoughts.todos.modules.DummyErrorHandler;
import pl.devthougths.todos.ProtobufTodoItem;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import play.api.http.ParserConfiguration;
import play.mvc.Http;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.devthoughts.todos.assertj.TodoItemRequestAssert.assertThat;
import static pl.devthoughts.todos.controllers.TodoItemRequest.DUE_DATE_FORMAT;
import static pl.devthoughts.todos.modules.protobuf.TodoItemRequestProtobufParser.NON_PROTOBUF_MIME_TYPE_ERR_MSG;
import static pl.devthoughts.todos.modules.protobuf.TodoItemRequestProtobufParser.NO_BODY_ERR_MSG;
import static pl.devthoughts.todos.modules.protobuf.TodoItemRequestProtobufParser.PROTOBUF_MIME_TYPE;

public class TodoItemRequestProtobufParserTest {

    private static final List<String> NON_PROTOBUF_MIMES =
        List.of("application/json", "text/plain", "text/xml");

    private static final ParserConfiguration
        PARSER_CONFIGURATION =
        new ParserConfiguration(10000, 10000L);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DUE_DATE_FORMAT);

    @Test
    public void should_reject_request_with_no_content_type_header() {
        Http.RequestHeader request = new Http.RequestBuilder().build();

        assertThatThrownBy(() -> bodyParser().parse(request, ByteString.empty()))
            .hasMessage(NON_PROTOBUF_MIME_TYPE_ERR_MSG);
    }

    @Test
    public void should_not_accept_non_protobuf_mime_type() {
        Http.RequestHeader request =
            new Http.RequestBuilder().header("Content-Type", nonProtobufMimeType()).build();

        assertThatThrownBy(() -> bodyParser().parse(request, ByteString.empty()))
            .hasMessage(NON_PROTOBUF_MIME_TYPE_ERR_MSG);
    }

    @Test
    public void should_reject_request_with_no_body() {

        Http.RequestHeader request =
            new Http.RequestBuilder().header("Content-Type", PROTOBUF_MIME_TYPE).build();

        assertThatThrownBy(() -> bodyParser().parse(request, ByteString.empty()))
            .hasMessage(NO_BODY_ERR_MSG);
    }

    @Test
    public void should_parse_request_with_protobuf_body() throws Exception {
        final byte[] todoItemAsBytes = ProtobufTodoItem.CreateItemRequest
            .newBuilder()
            .setName("Do something")
            .setDueDate(Timestamp.newBuilder().setSeconds(getSecondsFor("2017-09-16 23:59")))
            .build().toByteArray();
        ByteString bodyContent = ByteString.fromArray(todoItemAsBytes);

        Http.RequestHeader request =
            new Http.RequestBuilder()
                .bodyRaw(bodyContent)
                .header("Content-Type", PROTOBUF_MIME_TYPE)
                .build();

        final TodoItemRequest itemRequest = bodyParser().parse(request, bodyContent);

        assertThat(itemRequest)
            .hasName("Do something")
            .hasDueDate(fromString("2017-09-16 23:59"));
    }

    private long getSecondsFor(String date) {
        final LocalDateTime ldt = LocalDateTime.parse(date, FORMATTER);
        return ldt.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    @NotNull
    private TodoItemRequestProtobufParser bodyParser() {
        return new TodoItemRequestProtobufParser(PARSER_CONFIGURATION, new DummyErrorHandler());
    }

    @NotNull
    private LocalDateTime fromString(String date) {
        return LocalDateTime.parse(date, FORMATTER);
    }

    @NotNull
    private String nonProtobufMimeType() {
        return NON_PROTOBUF_MIMES.shuffle().head();
    }

}
