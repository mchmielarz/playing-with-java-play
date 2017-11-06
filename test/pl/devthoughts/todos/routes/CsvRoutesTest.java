package pl.devthoughts.todos.routes;

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
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.WithApplication;

import static com.revinate.assertj.json.JsonPathAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static play.test.Helpers.CREATED;
import static play.test.Helpers.GET;
import static play.test.Helpers.OK;
import static play.test.Helpers.POST;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;

public class CsvRoutesTest extends WithApplication {

    @Before
    public void setUp() {
        instanceOf(TodoItemRepository.class).removeAll();
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

        System.out.println(contentAsString(creationResult));

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

    private Result findSingleItem(String itemId) {
        return route(app, method(GET).uri("/todos/" + itemId));
    }

    @NotNull
    private Map<String, String> items(String name1, String dueDate1,
                                      String name2, String dueDate2) {
        return HashMap.of(name1, dueDate1, name2, dueDate2);
    }

    private String asCsv(Map<String, String> items) {
        return items.map(t -> t._1 + "," + t._2)
            .map(val -> val + '\n')
            .fold("name,dueDate\n", (acc, val) -> acc + val);
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
