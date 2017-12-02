package pl.devthoughts.todos.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.typesafe.config.ConfigFactory;

import org.junit.Before;
import org.junit.Test;

import pl.devthoughts.todos.repository.TodoItemRepository;

import play.Application;
import play.Mode;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.WithApplication;

import static com.revinate.assertj.json.JsonPathAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static play.test.Helpers.CREATED;
import static play.test.Helpers.DELETE;
import static play.test.Helpers.GET;
import static play.test.Helpers.NOT_FOUND;
import static play.test.Helpers.OK;
import static play.test.Helpers.POST;
import static play.test.Helpers.PUT;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;

public class RoutesTest extends WithApplication {

    @Before
    public void setUp() {
        instanceOf(TodoItemRepository.class).removeAll();
    }

    @Test
    public void should_create_single_todo_item() {
        Result creationResult = createItem(itemData("Do something", "2016-12-04T23:59:00"));

        assertThat(creationResult.status()).isEqualTo(CREATED);
        DocumentContext creationCtx = JsonPath.parse(contentAsString(creationResult));
        assertThat(creationCtx).jsonPathAsString("$.id").isNotEmpty();

        Result findingResult = findSingleItem(itemId(creationResult));

        assertThat(findingResult.status()).isEqualTo(OK);
        DocumentContext findingCtx = JsonPath.parse(contentAsString(findingResult));
        assertThat(findingCtx).jsonPathAsString("$.name").isEqualTo("Do something");
        assertThat(findingCtx).jsonPathAsString("$.dueDate").isEqualTo("2016-12-04T23:59:00");
        assertThat(findingCtx).jsonPathAsString("$.status").isEqualTo("OPEN");
    }

    @Test
    public void should_not_found_todo_item_for_unknown_id() {
        Result result = findSingleItem("unknown-id-3");
        assertThat(result.status()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void should_update_todo_item_with_new_data() {
        Result creationResult = createItem(itemData("Do something", "2016-12-04T23:59:00"));
        String itemId = itemId(creationResult);

        Result updateResult = editItem(itemId, itemData("Do nothing", "2016-12-25T23:59:00"));
        assertThat(updateResult.status()).isEqualTo(OK);

        Result findingResult = findSingleItem(itemId);
        assertThat(findingResult.status()).isEqualTo(OK);

        DocumentContext findingCtx = JsonPath.parse(contentAsString(findingResult));
        assertThat(findingCtx).jsonPathAsString("$.name").isEqualTo("Do nothing");
        assertThat(findingCtx).jsonPathAsString("$.dueDate").isEqualTo("2016-12-25T23:59:00");
        assertThat(findingCtx).jsonPathAsString("$.status").isEqualTo("OPEN");
    }

    @Test
    public void should_not_update_todo_item_for_unknown_id() {
        Result result = editItem("unknown-id-1", itemData("Do nothing", "2016-12-25T23:59:00"));
        assertThat(result.status()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void should_not_remove_todo_item_for_unknown_id() {
        Result result = deleteItem("unknown-id-2");
        assertThat(result.status()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void should_remove_todo_item() {
        Result creationResult = createItem(itemData("Do something", "2016-12-04T23:59:00"));
        String itemId = itemId(creationResult);

        deleteItem(itemId);

        Result findingResult = findSingleItem(itemId);
        assertThat(findingResult.status()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void should_find_all_todo_items() {
        createItem(itemData("Do something", "2016-12-04T23:59:00"));
        createItem(itemData("Send email", "2016-10-04T23:59:00"));

        Result result = route(app, method(GET).uri("/todos"));
        assertThat(result.status()).isEqualTo(OK);

        DocumentContext findingCtx = JsonPath.parse(contentAsString(result));
        assertThat(findingCtx).jsonPathAsString("$.items[0].name").isEqualTo("Do something");
        assertThat(findingCtx).jsonPathAsString("$.items[0].dueDate").isEqualTo("2016-12-04T23:59:00");
        assertThat(findingCtx).jsonPathAsString("$.items[1].name").isEqualTo("Send email");
        assertThat(findingCtx).jsonPathAsString("$.items[1].dueDate").isEqualTo("2016-10-04T23:59:00");
    }

    @Test
    public void should_mark_todo_item_as_done() {
        Result creationResult = createItem(itemData("Do something", "2016-12-04T23:59:00"));
        String itemId = itemId(creationResult);

        markItem(itemId, "done");

        Result findingResult = findSingleItem(itemId);
        assertThat(findingResult.status()).isEqualTo(OK);

        DocumentContext findingCtx = JsonPath.parse(contentAsString(findingResult));
        assertThat(findingCtx).jsonPathAsString("$.name").isEqualTo("Do something");
        assertThat(findingCtx).jsonPathAsString("$.dueDate").isEqualTo("2016-12-04T23:59:00");
        assertThat(findingCtx).jsonPathAsString("$.status").isEqualTo("DONE");
    }

    @Test
    public void should_marking_as_done_fail_for_unknown_id() {
        Result result = markItem("unknown-id", "done");
        assertThat(result.status()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void should_mark_todo_item_as_open() {
        Result creationResult = createItem(itemData("Do something", "2016-12-04T23:59:00"));
        String itemId = itemId(creationResult);

        markItem(itemId, "done");
        markItem(itemId, "open");

        Result findingResult = findSingleItem(itemId);
        assertThat(findingResult.status()).isEqualTo(OK);

        DocumentContext findingCtx = JsonPath.parse(contentAsString(findingResult));
        assertThat(findingCtx).jsonPathAsString("$.name").isEqualTo("Do something");
        assertThat(findingCtx).jsonPathAsString("$.dueDate").isEqualTo("2016-12-04T23:59:00");
        assertThat(findingCtx).jsonPathAsString("$.status").isEqualTo("OPEN");
    }

    @Test
    public void should_marking_as_open_fail_for_unknown_id() {
        Result result = markItem("unknown-id", "open");
        assertThat(result.status()).isEqualTo(NOT_FOUND);
    }

    private Result createItem(JsonNode itemData) {
        return route(app, method(POST).uri("/todos").bodyJson(itemData));
    }

    private Result findSingleItem(String itemId) {
        return route(app, method(GET).uri("/todos/" + itemId));
    }

    private Result editItem(String itemId, JsonNode newData) {
        return route(app, method(PUT)
            .uri("/todos/" + itemId)
            .bodyJson(newData));
    }

    private Result deleteItem(String itemId) {
        return route(app, method(DELETE).uri("/todos/" + itemId));
    }

    private JsonNode itemData(String name, String dueDate) {
        return Json.newObject()
            .put("name", name)
            .put("dueDate", dueDate);
    }

    private Result markItem(String itemId, String status) {
        RequestBuilder request = new RequestBuilder()
            .method(POST)
            .uri("/todos/" + itemId + "/" + status);
        return route(app, request);
    }

    private String itemId(Result result) {
        return Json.parse(contentAsString(result))
                   .path("id").asText();
    }

    private RequestBuilder method(String method) {
        return new RequestBuilder().method(method);
    }

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
            .withConfigLoader(env -> ConfigFactory.load("application-test.conf"))
            .in(Mode.TEST)
            .build();
    }
}
