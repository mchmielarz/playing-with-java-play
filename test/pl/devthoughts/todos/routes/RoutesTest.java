package pl.devthoughts.todos.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.typesafe.config.ConfigFactory;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

import org.jetbrains.annotations.NotNull;
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
        Result creationResult = createItem(itemData("Do something", "2016-12-04 23:59"));

        assertThat(creationResult.status()).isEqualTo(CREATED);
        DocumentContext creationCtx = JsonPath.parse(contentAsString(creationResult));
        assertThat(creationCtx).jsonPathAsString("$.id").isNotEmpty();

        Result findingResult = findSingleItem(itemId(creationResult));

        assertThat(findingResult.status()).isEqualTo(OK);
        DocumentContext findingCtx = JsonPath.parse(contentAsString(findingResult));
        assertThat(findingCtx).jsonPathAsString("$.name").isEqualTo("Do something");
        assertThat(findingCtx).jsonPathAsString("$.dueDate").isEqualTo("2016-12-04");
        assertThat(findingCtx).jsonPathAsString("$.status").isEqualTo("OPEN");
    }

    @Test
    public void should_create_multiple_todo_items_from_csv_request() {
        Result creationResult = route(app, method(POST)
            .uri("/csv/todos")
            .bodyText(asCsv(items(
                "Write an essay", "2017-09-16 23:59",
                "Replace a bulb", "2017-09-16 23:59")))
            .header("Content-Type", "text/csv")
        );

        assertThat(creationResult.status()).isEqualTo(CREATED);
        final String[] createdIds = contentAsString(creationResult).split("\n");
        assertThat(createdIds).hasSize(2);

        Result findingResult = findSingleItem(createdIds[0]);

        assertThat(findingResult.status()).isEqualTo(OK);
        DocumentContext findingCtx = JsonPath.parse(contentAsString(findingResult));
        assertThat(findingCtx).jsonPathAsString("$.name").isEqualTo("Write an essay");
        assertThat(findingCtx).jsonPathAsString("$.dueDate").isEqualTo("2017-09-16");
        assertThat(findingCtx).jsonPathAsString("$.status").isEqualTo("OPEN");

        Result findingResult2 = findSingleItem(createdIds[1]);

        assertThat(findingResult2.status()).isEqualTo(OK);
        DocumentContext findingCtx2 = JsonPath.parse(contentAsString(findingResult2));
        assertThat(findingCtx2).jsonPathAsString("$.name").isEqualTo("Replace a bulb");
        assertThat(findingCtx2).jsonPathAsString("$.dueDate").isEqualTo("2017-09-16");
        assertThat(findingCtx2).jsonPathAsString("$.status").isEqualTo("OPEN");
    }

    @Test
    public void should_not_found_todo_item_for_unknown_id() {
        Result result = findSingleItem("unknown-id-3");
        assertThat(result.status()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void should_update_todo_item_with_new_data() {
        Result creationResult = createItem(itemData("Do something", "2016-12-04 23:59"));
        String itemId = itemId(creationResult);

        Result updateResult = editItem(itemId, itemData("Do nothing", "2016-12-25 23:59"));
        assertThat(updateResult.status()).isEqualTo(OK);

        Result findingResult = findSingleItem(itemId);
        assertThat(findingResult.status()).isEqualTo(OK);

        DocumentContext findingCtx = JsonPath.parse(contentAsString(findingResult));
        assertThat(findingCtx).jsonPathAsString("$.name").isEqualTo("Do nothing");
        assertThat(findingCtx).jsonPathAsString("$.dueDate").isEqualTo("2016-12-25");
        assertThat(findingCtx).jsonPathAsString("$.status").isEqualTo("OPEN");
    }

    @Test
    public void should_not_update_todo_item_for_unknown_id() {
        Result result = editItem("unknown-id-1", itemData("Do nothing", "2016-12-25 23:59"));
        assertThat(result.status()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void should_not_remove_todo_item_for_unknown_id() {
        Result result = deleteItem("unknown-id-2");
        assertThat(result.status()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void should_remove_todo_item() {
        Result creationResult = createItem(itemData("Do something", "2016-12-04 23:59"));
        String itemId = itemId(creationResult);

        deleteItem(itemId);

        Result findingResult = findSingleItem(itemId);
        assertThat(findingResult.status()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void should_find_all_todo_items() {
        createItem(itemData("Do something", "2016-12-04 23:59"));
        createItem(itemData("Send email", "2016-10-04 23:59"));

        Result result = route(app, method(GET).uri("/todos"));
        assertThat(result.status()).isEqualTo(OK);

        DocumentContext findingCtx = JsonPath.parse(contentAsString(result));
        assertThat(findingCtx).jsonPathAsString("$.items[0].name").isEqualTo("Do something");
        assertThat(findingCtx).jsonPathAsString("$.items[0].dueDate").isEqualTo("2016-12-04");
        assertThat(findingCtx).jsonPathAsString("$.items[1].name").isEqualTo("Send email");
        assertThat(findingCtx).jsonPathAsString("$.items[1].dueDate").isEqualTo("2016-10-04");
    }

    @Test
    public void should_mark_todo_item_as_done() {
        Result creationResult = createItem(itemData("Do something", "2016-12-04 23:59"));
        String itemId = itemId(creationResult);

        markItem(itemId, "done");

        Result findingResult = findSingleItem(itemId);
        assertThat(findingResult.status()).isEqualTo(OK);

        DocumentContext findingCtx = JsonPath.parse(contentAsString(findingResult));
        assertThat(findingCtx).jsonPathAsString("$.name").isEqualTo("Do something");
        assertThat(findingCtx).jsonPathAsString("$.dueDate").isEqualTo("2016-12-04");
        assertThat(findingCtx).jsonPathAsString("$.status").isEqualTo("DONE");
    }

    @Test
    public void should_marking_as_done_fail_for_unknown_id() {
        Result result = markItem("unknown-id", "done");
        assertThat(result.status()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void should_mark_todo_item_as_open() {
        Result creationResult = createItem(itemData("Do something", "2016-12-04 23:59"));
        String itemId = itemId(creationResult);

        markItem(itemId, "done");
        markItem(itemId, "open");

        Result findingResult = findSingleItem(itemId);
        assertThat(findingResult.status()).isEqualTo(OK);

        DocumentContext findingCtx = JsonPath.parse(contentAsString(findingResult));
        assertThat(findingCtx).jsonPathAsString("$.name").isEqualTo("Do something");
        assertThat(findingCtx).jsonPathAsString("$.dueDate").isEqualTo("2016-12-04");
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

    @NotNull
    private Map<String, String> items(String name1, String dueDate1,
                                      String name2, String dueDate2) {
        return HashMap.of(name1, dueDate1, name2, dueDate2);
    }

    private JsonNode itemData(String name, String dueDate) {
        return Json.newObject()
            .put("name", name)
            .put("dueDate", dueDate);
    }

    private String asCsv(Map<String, String> items) {
        return items.map(t -> t._1 + "," + t._2)
            .map(val -> val + '\n')
            .fold("name,dueDate\n", (acc, val) -> acc + val);
    }

    private Result markItem(String itemId, String status) {
        RequestBuilder request = new RequestBuilder()
            .method(POST)
            .uri("/todos/" + itemId + "/" + status);
        return route(request);
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
