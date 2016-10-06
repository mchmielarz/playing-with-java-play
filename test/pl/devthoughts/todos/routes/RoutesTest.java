package pl.devthoughts.todos.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.WithApplication;

import static org.assertj.core.api.Assertions.assertThat;
import static com.revinate.assertj.json.JsonPathAssert.assertThat;
import static play.test.Helpers.*;

public class RoutesTest extends WithApplication {

    @Test
    public void should_create_single_todo_item() {
        Result creationResult = route(method(POST)
                                        .uri("/todos")
                                        .bodyJson(itemData("Do something", "2016-12-04"))
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
    public void should_not_found_todo_item_for_unknown_id() {
        Result result = route(method(GET)
                                .uri("/todos/unknown-id-3"));
        assertThat(result.status()).isEqualTo(NOT_FOUND);
    }

    @Test
    public void should_update_todo_item_with_new_data() {
        Result creationResult = route(method(POST)
                                        .uri("/todos")
                                        .bodyJson(itemData("Do something", "2016-12-04"))
        );
        String itemId = itemId(creationResult);

        Result updateResult = route(method(PUT)
                                    .uri("/todos/" + itemId)
                                    .bodyJson(itemData("Do nothing", "2016-12-25")));
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
                                .bodyJson(itemData("Do nothing", "2016-12-25")));
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
                                        .bodyJson(itemData("Do something", "2016-12-04"))
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
            .bodyJson(itemData("Do something", "2016-12-04"))
        );
        route(method(POST)
            .uri("/todos")
            .bodyJson(itemData("Send email", "2016-10-04"))
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
                                        .bodyJson(itemData("Do something", "2016-12-04"))
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
                                        .bodyJson(itemData("Do something", "2016-12-04"))
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
