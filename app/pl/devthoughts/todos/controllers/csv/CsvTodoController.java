package pl.devthoughts.todos.controllers.csv;

import io.vavr.Value;
import pl.devthoughts.todos.controllers.TodoItemRequest;
import pl.devthoughts.todos.domain.TodoItemId;
import pl.devthoughts.todos.modules.csv.OpenCsvBodyParser;
import pl.devthoughts.todos.service.TodoService;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

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
            .map(todoService::saveItem)
            .flatMap(Value::toJavaStream)
            .map(TodoItemId::getId)
            .collect(Collectors.joining("\n"));
        return created(createdIds);
    }
}
