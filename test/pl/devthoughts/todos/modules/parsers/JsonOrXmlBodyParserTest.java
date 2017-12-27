package pl.devthoughts.todos.modules.parsers;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.revinate.assertj.json.JsonPathAssert;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import pl.devthoughts.todos.TimeUtils;
import pl.devthoughts.todos.controllers.TodoItemRequest;
import pl.devthoughts.todos.modules.JsonOrXmlBodyParser;
import play.api.http.HttpConfiguration;
import play.http.HttpErrorHandler;
import play.libs.F;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import java.time.LocalDateTime;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static pl.devthoughts.todos.assertj.TodoItemRequestAssert.assertThat;
import static play.test.Helpers.contentAsString;

public class JsonOrXmlBodyParserTest {

  private ActorSystem system = ActorSystem.create();
  private Materializer mat = ActorMaterializer.create(system);

  private HttpErrorHandler errorHandler = mock(HttpErrorHandler.class);
  private HttpConfiguration config = HttpConfiguration.createWithDefaults();

  private BodyParser.TolerantJson jsonParser = new BodyParser.TolerantJson(config, errorHandler);
  private BodyParser.TolerantXml xmlParser = new BodyParser.TolerantXml(config, errorHandler);

  private Executor executor = Executors.newSingleThreadExecutor();

  private BodyParser<TodoItemRequest> parser = new JsonOrXmlBodyParser(jsonParser, xmlParser, executor);

  @Test
  public void should_rise_error_for_request_with_no_Content_Type_header() throws Exception {
    parser
        .apply(Helpers.fakeRequest().build())
        .run(mat)
        .whenComplete(
            isJsonBadRequest("No Content-Type provided in the request.")
        )
        .toCompletableFuture()
        .get();
  }

  @Test
  public void should_rise_error_for_request_with_not_supported_Content_Type_header() throws Exception {
    parser
        .apply(Helpers.fakeRequest().header("Content-Type", "text/plain").build())
        .run(mat)
        .whenComplete(
            isJsonBadRequest("Unsupported Content-Type provided in the request.")
        )
        .toCompletableFuture()
        .get();
  }

  @Test
  public void should_parse_request_with_json_content() throws Exception {
    final ObjectNode objectNode = JsonNodeFactory.instance
        .objectNode()
        .put("name", "Do something")
        .put("dueDate", "2017-09-16T23:59:00");

    parser
        .apply(jsonRequest())
        .run(ByteString.fromString(objectNode.toString()), mat)
        .whenComplete(
            isTodoItemRequest("Do something", TimeUtils.fromString("2017-09-16T23:59:00"))
        )
        .toCompletableFuture()
        .get();
  }

  @Test
  public void should_parse_request_with_xml_content() throws Exception {
    final String xmlData = "<item><name>Do something</name><dueDate>2017-09-16T23:59:00</dueDate></item>";

    parser
        .apply(xmlRequest())
        .run(ByteString.fromString(xmlData), mat)
        .whenComplete(
            isTodoItemRequest("Do something", TimeUtils.fromString("2017-09-16T23:59:00"))
        )
        .toCompletableFuture()
        .get();
  }

  private Http.RequestImpl jsonRequest() {
    return Helpers.fakeRequest().header("Content-Type", "application/json").build();
  }

  private Http.RequestImpl xmlRequest() {
    return Helpers.fakeRequest().header("Content-Type", "text/xml").build();
  }

  @NotNull private BiConsumer<F.Either<Result, TodoItemRequest>, Throwable> isJsonBadRequest(
      String failureReason) {
    return (either, ex) -> {
      final Result result = either.left.get();

      assertThat(result.status()).isEqualTo(Http.Status.BAD_REQUEST);
      assertThat(result.body().contentType()).hasValue("application/json; charset=UTF-8");

      DocumentContext parsingResult = JsonPath.parse(contentAsString(result));
      JsonPathAssert.assertThat(parsingResult).jsonPathAsString("$.reason")
                    .isEqualTo(failureReason);
    };
  }

  @NotNull private BiConsumer<F.Either<Result, TodoItemRequest>, Throwable> isTodoItemRequest(
      String expectedName, LocalDateTime expectedDueDate
  ) {
    return (either, ex) -> {
      final TodoItemRequest itemRequest = either.right.get();
      assertThat(itemRequest)
          .hasName(expectedName)
          .hasDueDate(expectedDueDate);
    };
  }

}
