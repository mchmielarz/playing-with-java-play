package pl.devthoughts.todos.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import pl.devthoughts.todos.repository.TodoItemRepository;

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
        Result creationResult = route(method(POST)
                                        .uri("/todos")
                                        .bodyJson(itemData("Do something", "2016-12-04  23:59"))
        );

        assertThat(creationResult.status()).isEqualTo(CREATED);
        DocumentContext creationCtx = JsonPath.parse(contentAsString(creationResult));
        assertThat(creationCtx).jsonPathAsString("$.id").isNotEmpty();

        Result findingResult = route(method(GET)
                                    .uri("/todos/" + itemId(creationResult))
        );

        assertThat(findingResult.status()).isEqualTo(OK);
        DocumentContext findingCtx = JsonPath.parse(contentAsString(findingResult));
        assertThat(findingCtx).jsonPathAsString("$.name").isEqualTo("Do something");
        assertThat(findingCtx).jsonPathAsString("$.dueDate").isEqualTo("2016-12-04");
        assertThat(findingCtx).jsonPathAsString("$.status").isEqualTo("OPEN");
    }

    @Test
    public void should_create_multiple_todo_items_from_csv_request() {
        Result creationResult = route(method(POST)
            .uri("/todos/csv")
            .bodyText(asCsv(items(
                "Maybe yes", "2017-09-16 23:59",
                "Or maybe no", "2017-09-16 23:59")))
            .header("Content-Type", "text/csv")
        );

        assertThat(creationResult.status()).isEqualTo(CREATED);
        final String[] createdIds = contentAsString(creationResult).split("\n");
        assertThat(createdIds).hasSize(2);

        Result findingResult = route(method(GET)
            .uri("/todos/" + createdIds[0])
        );

        assertThat(findingResult.status()).isEqualTo(OK);
        DocumentContext findingCtx = JsonPath.parse(contentAsString(findingResult));
        assertThat(findingCtx).jsonPathAsString("$.name").isEqualTo("Maybe yes");
        assertThat(findingCtx).jsonPathAsString("$.dueDate").isEqualTo("2017-09-16");
        assertThat(findingCtx).jsonPathAsString("$.status").isEqualTo("OPEN");

        Result findingResult2 = route(method(GET)
            .uri("/todos/" + createdIds[1])
        );

        assertThat(findingResult2.status()).isEqualTo(OK);
        DocumentContext findingCtx2 = JsonPath.parse(contentAsString(findingResult2));
        assertThat(findingCtx2).jsonPathAsString("$.name").isEqualTo("Or maybe no");
        assertThat(findingCtx2).jsonPathAsString("$.dueDate").isEqualTo("2017-09-16");
        assertThat(findingCtx2).jsonPathAsString("$.status").isEqualTo("OPEN");
    }

    @NotNull
    private Map<String, String> items(String name1, String dueDate1,
                                      String name2, String dueDate2) {
        return HashMap.of(name1, dueDate1, name2, dueDate2);
    }

    @Test
    public void should_not_found_todo_item_for_unknown_id() {
        Result result = route(method(GET)
                                .uri("/todos/unknown-id-3"));
        assertThat(result.status()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void should_update_todo_item_with_new_data() {
        Result creationResult = route(method(POST)
                                        .uri("/todos")
                                        .bodyJson(itemData("Do something", "2016-12-04 23:59"))
        );
        String itemId = itemId(creationResult);

        Result updateResult = route(method(PUT)
                                    .uri("/todos/" + itemId)
                                    .bodyJson(itemData("Do nothing", "2016-12-25 23:59")));
        assertThat(updateResult.status()).isEqualTo(OK);

        Result findingResult = route(method(GET)
                                    .uri("/todos/" + itemId));
        assertThat(findingResult.status()).isEqualTo(OK);
        DocumentContext findingCtx = JsonPath.parse(contentAsString(findingResult));
        assertThat(findingCtx).jsonPathAsString("$.name").isEqualTo("Do nothing");
        assertThat(findingCtx).jsonPathAsString("$.dueDate").isEqualTo("2016-12-25");
        assertThat(findingCtx).jsonPathAsString("$.status").isEqualTo("OPEN");
    }

    @Test
    public void should_not_update_todo_item_for_unknown_id() {
        Result result = route(method(PUT)
                                .uri("/todos/unknown-id-1")
                                .bodyJson(itemData("Do nothing", "2016-12-25 23:59")));
        assertThat(result.status()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void should_not_remove_todo_item_for_unknown_id() {
        Result result = route(method(DELETE)
                                .uri("/todos/unknown-id-2"));
        assertThat(result.status()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void should_remove_todo_item() {
        Result creationResult = route(method(POST)
                                        .uri("/todos")
                                        .bodyJson(itemData("Do something", "2016-12-04 23:59"))
        );
        String itemId = itemId(creationResult);

        route(method(DELETE)
                .uri("/todos/" + itemId));

        Result findingResult = route(method(GET)
                                    .uri("/todos/" + itemId));
        assertThat(findingResult.status()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void should_find_all_todo_items() {
        route(method(POST)
            .uri("/todos")
            .bodyJson(itemData("Do something", "2016-12-04 23:59"))
        );
        route(method(POST)
            .uri("/todos")
            .bodyJson(itemData("Send email", "2016-10-04 23:59"))
        );

        Result result = route(method(GET)
                              .uri("/todos"));
        assertThat(result.status()).isEqualTo(OK);
        DocumentContext findingCtx = JsonPath.parse(contentAsString(result));
        assertThat(findingCtx).jsonPathAsString("$.items[0].name").isEqualTo("Do something");
        assertThat(findingCtx).jsonPathAsString("$.items[0].dueDate").isEqualTo("2016-12-04");
        assertThat(findingCtx).jsonPathAsString("$.items[1].name").isEqualTo("Send email");
        assertThat(findingCtx).jsonPathAsString("$.items[1].dueDate").isEqualTo("2016-10-04");
    }

    @Test
    public void should_mark_todo_item_as_done() {
        Result creationResult = route(method(POST)
                                        .uri("/todos")
                                        .bodyJson(itemData("Do something", "2016-12-04 23:59"))
        );
        String itemId = itemId(creationResult);

        markItem(itemId, "done");

        Result findingResult = route(method(GET)
                                    .uri("/todos/" + itemId));
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
        Result creationResult = route(method(POST)
                                        .uri("/todos")
                                        .bodyJson(itemData("Do something", "2016-12-04 23:59"))
        );
        String itemId = itemId(creationResult);

        markItem(itemId, "done");
        markItem(itemId, "open");

        Result findingResult = route(method(GET)
                                    .uri("/todos/" + itemId));
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

    private JsonNode itemData(String name, String dueDate) {
        return Json.newObject()
            .put("name", name)
            .put("dueDate", dueDate);
    }

    private String asCsv(Map<String, String> items) {
        return items.map(t -> t._1 + "," + t._2)
            .map(val -> val + '\n')
            .fold("", (acc, val) -> acc + val);
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

}
