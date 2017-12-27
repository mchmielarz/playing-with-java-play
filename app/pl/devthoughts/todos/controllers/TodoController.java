package pl.devthoughts.todos.controllers;

import pl.devthoughts.todos.domain.TodoItemId;
import pl.devthoughts.todos.domain.TodoItems;
import pl.devthoughts.todos.modules.JsonOrXmlBodyParser;
import pl.devthoughts.todos.service.TodoService;
import play.libs.Jsonp;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Content;

import javax.inject.Inject;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Patterns.$Failure;
import static io.vavr.Patterns.$None;
import static io.vavr.Patterns.$Some;
import static io.vavr.Patterns.$Success;
import static play.libs.Json.toJson;

public class TodoController extends Controller {

    private final TodoService todoService;

    @Inject
    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @BodyParser.Of(JsonOrXmlBodyParser.class)
    public Result addItem() {
        TodoItemRequest request = getRequest();
        return todoService.saveItem(request)
            .map(itemId -> created(content(itemId)))
            .getOrElse(internalServerError());
    }

    public Result getItem(String id) {
        return todoService.findItem(id)
            .transform(t -> Match(t).of(
                Case($Some($()), item -> ok(toJson(item))),
                Case($None(), () -> notFound())
            ));
    }

    @BodyParser.Of(JsonOrXmlBodyParser.class)
    public Result updateItem(String id) {
        final TodoItemRequest req = getRequest();
        return todoService.updateItem(id, req)
            .transform(t -> Match(t).of(
                Case($Success($()), item -> ok()),
                Case($Failure($()), ex -> notFound())
            ));
    }

    public Result deleteItem(String id) {
        return todoService.deleteItem(id)
            .transform(t -> Match(t).of(
                Case($Success($()), item -> ok()),
                Case($Failure($()), ex -> notFound())
            ));
    }

    public Result done(String id) {
        return todoService.done(id)
            .transform(t -> Match(t).of(
                Case($Success($()), item -> ok()),
                Case($Failure($()), ex -> notFound())
            ));
    }

    public Result reopen(String id) {
        return todoService.reopen(id)
            .transform(t -> Match(t).of(
                Case($Success($()), item -> ok()),
                Case($Failure($()), ex -> notFound())
            ));
    }

    public Result getAllItems() {
        final TodoItems items = todoService.getAllItems();
        return ok(toJson(items));
    }

    private TodoItemRequest getRequest() {
        return request().body().as(TodoItemRequest.class);
    }

    private Content content(TodoItemId itemId) {
        final String contentType = request()
            .contentType()
            .orElse("application/json");

        if (contentType.equals("text/xml") || contentType.equals("application/xml") || contentType.matches("application/.*\\+xml.*")) {
            return new Content() {
                @Override
                public String body() {
                    return String.format("<itemId>%s</itemId>", itemId.getId());
                }

                @Override
                public String contentType() {
                    return "text/xml";
                }
            };
        } else {
            return Jsonp.jsonp("", toJson(itemId));
        }
    }
}
