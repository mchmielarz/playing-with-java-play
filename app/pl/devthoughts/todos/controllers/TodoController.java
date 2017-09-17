package pl.devthoughts.todos.controllers;

import com.fasterxml.jackson.databind.JsonNode;

import pl.devthoughts.todos.domain.TodoItems;
import pl.devthoughts.todos.modules.CsvBodyParser;
import pl.devthoughts.todos.service.TodoService;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Patterns.$Failure;
import static io.vavr.Patterns.$None;
import static io.vavr.Patterns.$Some;
import static io.vavr.Patterns.$Success;
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
            .map(itemId -> created(toJson(itemId)))
            .getOrElse(internalServerError());
    }

    @BodyParser.Of(CsvBodyParser.class)
    public Result addItems() {
        final List<TodoItemRequest> list = request().body().as(List.class);
        final String createdIds = list.stream()
            .map(req -> todoService.saveItem(req))
            .flatMap(optId -> optId.toJavaStream())
            .map(id -> id.getId())
            .collect(Collectors.joining("\n"));
        return created(createdIds);
    }

    public Result getItem(String id) {
        return todoService.findItem(id)
            .transform(t -> Match(t).of(
                Case($Some($()), item -> ok(toJson(item))),
                Case($None(), () -> notFound())
            ));
    }

    @BodyParser.Of(BodyParser.Json.class)
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
        JsonNode json = request().body().asJson();
        return fromJson(json, TodoItemRequest.class);
    }
}
