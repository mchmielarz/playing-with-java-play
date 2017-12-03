package pl.devthoughts.todos.routes;

import akka.util.ByteString;

import com.google.protobuf.InvalidProtocolBufferException;
import com.typesafe.config.ConfigFactory;

import io.vavr.collection.List;
import io.vavr.control.Try;

import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import pl.devthoughts.todos.repository.TodoItemRepository;
import pl.devthougths.todos.ProtobufTodoItem;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

import play.Application;
import play.Logger;
import play.Mode;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.devthoughts.todos.TimeUtils.asTimestamp;
import static pl.devthoughts.todos.TodosConfig.DUE_DATE_FORMAT;
import static pl.devthoughts.todos.modules.protobuf.TodoItemRequestProtobufParser.PROTOBUF_MIME_TYPE;
import static play.mvc.Http.Status.CREATED;
import static play.test.Helpers.GET;
import static play.test.Helpers.OK;
import static play.test.Helpers.POST;
import static play.test.Helpers.route;

public class ProtobufRoutesTest extends WithApplication {

    private static final Logger.ALogger LOGGER = Logger.of(ProtobufRoutesTest.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DUE_DATE_FORMAT);

    @Before
    public void setUp() {
        instanceOf(TodoItemRepository.class).removeAll();
    }

    @Ignore
    @Test
    public void should_create_todo_items_with_protobuf()
        throws InvalidProtocolBufferException, ExecutionException, InterruptedException {
        // given
        final List<ByteString> itemsData = List.of(
            newItemData("Do something", "2016-12-04 23:59"),
            newItemData("Time for rest!", "2016-12-05 23:59")
        );

        // when
        final Try<List<Result>> confirmedCreation = Try.of(() -> itemsData)
            .mapTry(this::createItems)
            .mapTry(this::assertCreated);

        // then
        final ProtobufTodoItem.FetchItemsRequest fetchItemsRequest = confirmedCreation
            .map(this::extractCreationResponses)
            .map(this::extractItemIds)
            .map(this::asFetchItemsRequest)
            .onFailure(ex -> LOGGER.error("Cannot create request to fetch multiple items", ex))
            .get();

        final ByteString multipleIds = ByteString.fromArray(fetchItemsRequest.toByteArray());
        final Result result = findItems(multipleIds);
        assertThat(result.status()).isEqualTo(OK);

        ProtobufTodoItem.FetchItemsResponse resp = extractFetchItemsResponse(result);
        assertThat(resp.getItemList()).hasSize(2);
        assertThat(resp.getItemList())
            .extracting("name", "dueDate", "status")
            .contains(
                Tuple.tuple("Do something", asTimestamp("2016-12-04 23:59"), "OPEN"),
                Tuple.tuple("Time for rest!", asTimestamp("2016-12-05 23:59"), "OPEN")
            );
    }

    private ProtobufTodoItem.FetchItemsResponse.Item item(String name, String dueDate, String status) {
        return ProtobufTodoItem.FetchItemsResponse.Item.newBuilder()
            .setName(name)
            .setDueDate(asTimestamp(dueDate))
            .setStatus(status)
            .build();
    }

    private ProtobufTodoItem.FetchItemsResponse extractFetchItemsResponse(Result result) {
        try {
            final ByteString bytes = result.body().consumeData(mat).toCompletableFuture().get();
            return ProtobufTodoItem.FetchItemsResponse.parseFrom(bytes.toArray());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot extract CreateItemResponse from call result.", e);
        }
    }

    private ProtobufTodoItem.FetchItemsRequest asFetchItemsRequest(List<String> ids) {
        return
            ProtobufTodoItem.FetchItemsRequest.newBuilder()
            .addAllId(ids)
            .build();
    }

    private List<String> extractItemIds(List<ProtobufTodoItem.CreateItemResponse> creationResponses) {
        return creationResponses.map(ProtobufTodoItem.CreateItemResponse::getId);
    }

    private List<ProtobufTodoItem.CreateItemResponse> extractCreationResponses(List<Result> results) {
        return results.map(this::extractCreateItemResponse);
    }

    private List<Result> assertCreated(List<Result> results) {
        results.forEach(result -> assertThat(result.status()).isEqualTo(CREATED));
        return results;
    }

    private List<Result> createItems(List<ByteString> itemsData) {
        return itemsData.map(this::createItem);
    }

    private ProtobufTodoItem.CreateItemResponse extractCreateItemResponse(Result creationResult) {
        try {
            final ByteString bytes = creationResult.body().consumeData(mat).toCompletableFuture().get();
            return ProtobufTodoItem.CreateItemResponse.parseFrom(bytes.toArray());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot extract CreateItemResponse from call result.", e);
        }
    }

    private Result createItem(ByteString itemData) {
        return route(app,
            method(POST)
                .uri("/proto/todos")
                .bodyRaw(itemData)
                .header("Content-Type", PROTOBUF_MIME_TYPE));
    }

    private Result findItems(ByteString ids) {
        return route(app,
            method(GET)
                .uri("/proto/todos")
                .bodyRaw(ids)
                .header("Content-Type", PROTOBUF_MIME_TYPE));
    }

    private ByteString newItemData(String name, String dueDate) {
        final byte[] todoItemAsBytes = ProtobufTodoItem.CreateItemRequest
            .newBuilder()
            .setName(name)
            .setDueDate(asTimestamp(dueDate))
            .build().toByteArray();
        return ByteString.fromArray(todoItemAsBytes);
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
