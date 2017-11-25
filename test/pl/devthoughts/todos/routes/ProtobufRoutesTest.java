package pl.devthoughts.todos.routes;

import akka.util.ByteString;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.revinate.assertj.json.JsonPathAssert;
import com.typesafe.config.ConfigFactory;

import org.junit.Before;
import org.junit.Test;

import pl.devthoughts.todos.repository.TodoItemRepository;
import pl.devthougths.todos.ProtobufTodoItem;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

import play.Application;
import play.Mode;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.devthoughts.todos.controllers.TodoItemRequest.DUE_DATE_FORMAT;
import static pl.devthoughts.todos.modules.protobuf.TodoItemRequestProtobufParser.PROTOBUF_MIME_TYPE;
import static play.test.Helpers.CREATED;
import static play.test.Helpers.GET;
import static play.test.Helpers.OK;
import static play.test.Helpers.POST;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;

public class ProtobufRoutesTest extends WithApplication {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DUE_DATE_FORMAT);

    @Before
    public void setUp() {
        instanceOf(TodoItemRepository.class).removeAll();
    }

    @Test
    public void should_create_single_todo_item()
        throws InvalidProtocolBufferException, ExecutionException, InterruptedException {
        Result creationResult = createItem(itemData("Do something", "2016-12-04 23:59"));
        assertThat(creationResult.status()).isEqualTo(CREATED);

        final ProtobufTodoItem.CreateItemResponse response =
            extractCreateItemResponse(creationResult);
        assertThat(response.getId()).isNotEmpty();

        Result findingResult = findSingleItem(response.getId());

        assertThat(findingResult.status()).isEqualTo(OK);
        DocumentContext findingCtx = JsonPath.parse(contentAsString(findingResult));
        JsonPathAssert.assertThat(findingCtx).jsonPathAsString("$.name").isEqualTo("Do something");
        JsonPathAssert.assertThat(findingCtx).jsonPathAsString("$.dueDate").isEqualTo("2016-12-04");
        JsonPathAssert.assertThat(findingCtx).jsonPathAsString("$.status").isEqualTo("OPEN");
    }

    private ProtobufTodoItem.CreateItemResponse extractCreateItemResponse(Result creationResult)
        throws InterruptedException, ExecutionException,
        InvalidProtocolBufferException {
        final ByteString bytes =
            creationResult.body().consumeData(mat).toCompletableFuture().get();
        return ProtobufTodoItem.CreateItemResponse.parseFrom(bytes.toArray());
    }

    private Result createItem(ByteString itemData) {
        return route(app,
            method(POST)
                .uri("/proto/todos")
                .bodyRaw(itemData)
                .header("Content-Type", PROTOBUF_MIME_TYPE));
    }

    private Result findSingleItem(String itemId) {
        return route(app, method(GET).uri("/todos/" + itemId));
    }

    private ByteString itemData(String name, String dueDate) {
        final byte[] todoItemAsBytes = ProtobufTodoItem.CreateItemRequest
            .newBuilder()
            .setName(name)
            .setDueDate(Timestamp.newBuilder().setSeconds(getSecondsFor(dueDate)))
            .build().toByteArray();
        return ByteString.fromArray(todoItemAsBytes);
    }

    private long getSecondsFor(String date) {
        final LocalDateTime ldt = LocalDateTime.parse(date, FORMATTER);
        return ldt.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    private Http.RequestBuilder method(String method) {
        return new Http.RequestBuilder().method(method);
    }

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
            .withConfigLoader(env -> ConfigFactory.load("application-test.conf"))
            .in(Mode.TEST)
            .build();
    }
}
