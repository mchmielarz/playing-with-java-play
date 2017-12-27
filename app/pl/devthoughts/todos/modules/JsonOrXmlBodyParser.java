package pl.devthoughts.todos.modules;

import akka.util.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import pl.devthoughts.todos.TimeUtils;
import pl.devthoughts.todos.controllers.TodoItemRequest;
import play.libs.F;
import play.libs.streams.Accumulator;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.util.Optional;
import java.util.concurrent.Executor;

public class JsonOrXmlBodyParser implements BodyParser<TodoItemRequest> {

  private final TolerantJson jsonDelegate;
  private final TolerantXml xmlDelegate;
  private final Executor executor;

  @Inject
  public JsonOrXmlBodyParser(TolerantJson jsonDelegate, TolerantXml xmlDelegate,
                             Executor executor) {
    this.jsonDelegate = jsonDelegate;
    this.xmlDelegate = xmlDelegate;
    this.executor = executor;
  }

  @Override public Accumulator<ByteString, F.Either<Result, TodoItemRequest>> apply(Http.RequestHeader request) {
    final Optional<String> contentTypeOpt = request.contentType();
    if (contentTypeOpt.isPresent()) {
      String contentType = contentTypeOpt.get();
      if (isJsonType(contentType)) {
        return parseJson(request);
      } else if (isXmlType(contentType)) {
        return parseXml(request);
      } else {
        return Accumulator.done(badRequest("Unsupported Content-Type provided in the request."));
      }
    } else {
      return Accumulator.done(badRequest("No Content-Type provided in the request."));
    }
  }

  private boolean isJsonType(String contentType) {
    return contentType.equals("text/json") || contentType.equals("application/json");
  }

  private boolean isXmlType(String contentType) {
    return
        contentType.equals("text/xml") || contentType.equals("application/xml") || contentType.matches("application/.*\\+xml.*");
  }

  private Accumulator<ByteString, F.Either<Result, TodoItemRequest>> parseJson(
      Http.RequestHeader request) {
    return jsonDelegate.apply(request)
                     .map(resultOrJson -> {
                       if (resultOrJson.left.isPresent()) {
                         return F.Either.Left(resultOrJson.left.get());
                       } else {
                         JsonNode json = resultOrJson.right.get();
                         try {
                           TodoItemRequest item = play.libs.Json.fromJson(json, TodoItemRequest.class);
                           return F.Either.Right(item);
                         } catch (Exception e) {
                           return badRequest("Unable to read Todo item request from json: " + e.getMessage());
                         }
                       }
                     }, executor);
  }

  private Accumulator<ByteString, F.Either<Result, TodoItemRequest>> parseXml(
      Http.RequestHeader request) {
    return xmlDelegate.apply(request)
                    .map(resultOrXml -> {
                      if (resultOrXml.left.isPresent()) {
                        return F.Either.Left(resultOrXml.left.get());
                      } else {
                        Document xml = resultOrXml.right.get();
                        try {
                          System.out.println("XML: " + xml);
                          String name = xml.getElementsByTagName("name").item(0).getTextContent();
                          String dueDate = xml.getElementsByTagName("dueDate").item(0).getTextContent();
                          TodoItemRequest item = new TodoItemRequest(name, TimeUtils.fromString(dueDate));
                          return F.Either.Right(item);
                        } catch (Exception e) {
                          return F.Either.Left(Results.badRequest(
                              "Unable to read Todo item request from xml: " + e.getMessage()));
                        }
                      }
                    }, executor);
  }

  @NotNull private F.Either<Result, TodoItemRequest> badRequest(
      String message) {
    final ObjectNode node = JsonNodeFactory.instance.objectNode().put("reason", message);
    return F.Either.Left(Results.badRequest(node));
  }

}
