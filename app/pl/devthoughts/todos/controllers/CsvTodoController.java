package pl.devthoughts.todos.controllers;

import pl.devthoughts.todos.modules.OpenCsvBodyParser;
import pl.devthoughts.todos.service.TodoService;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

public class CsvTodoController extends Controller {

    private final TodoService todoService;

    @Inject
    public CsvTodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @BodyParser.Of(OpenCsvBodyParser.class)
    public Result addItems() {
        final List<TodoItemRequest> list = request().body().as(List.class);
        final String createdIds = list.stream()
            .map(req -> todoService.saveItem(req))
            .flatMap(optId -> optId.toJavaStream())
            .map(id -> id.getId())
            .collect(Collectors.joining("\n"));
        return created(createdIds);
    }
}
