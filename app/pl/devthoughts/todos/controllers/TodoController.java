package pl.devthoughts.todos.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import pl.devthoughts.todos.domain.TodoItems;
import pl.devthoughts.todos.service.TodoService;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

import static javaslang.API.$;
import static javaslang.API.Case;
import static javaslang.API.Match;
import static javaslang.Patterns.Failure;
import static javaslang.Patterns.None;
import static javaslang.Patterns.Some;
import static javaslang.Patterns.Success;
import static play.libs.Json.fromJson;
import static play.libs.Json.toJson;

public class TodoController extends Controller {

    private final TodoService todoService;

    @Inject
    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result addItem() {
        TodoItemRequest request = getRequest();
        return todoService.saveItem(request)
            .toOption()
            .map(itemId -> created(toJson(itemId)))
            .getOrElse(notFound());
    }

    public Result getItem(String id) {
        return todoService.findItem(id)
            .transform(t -> Match(t).of(
                Case(Some($()), item -> ok(toJson(item))),
                Case(None(), () -> notFound())
            ));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result updateItem(String id) {
        final TodoItemRequest req = getRequest();
        return todoService.updateItem(id, req)
            .transform(t -> Match(t).of(
                Case(Success($()), item -> ok()),
                Case(Failure($()), ex -> notFound())
            ));
    }

    public Result deleteItem(String id) {
        return todoService.deleteItem(id)
            .transform(t -> Match(t).of(
                Case(Success($()), item -> ok()),
                Case(Failure($()), ex -> notFound())
            ));
    }

    public Result done(String id) {
        return todoService.done(id)
            .transform(t -> Match(t).of(
                Case(Success($()), item -> ok()),
                Case(Failure($()), ex -> notFound())
            ));
    }

    public Result reopen(String id) {
        return todoService.reopen(id)
            .transform(t -> Match(t).of(
                Case(Success($()), item -> ok()),
                Case(Failure($()), ex -> notFound())
            ));
    }

    public Result getAllItems() {
        final TodoItems items = todoService.getAllItems();
        return ok(toJson(items));
    }

    private TodoItemRequest getRequest() {
        JsonNode json = request().body().asJson();
        return fromJson(json, TodoItemRequest.class);
    }

}
