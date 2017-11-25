package pl.devthoughts.todos.controllers;

import pl.devthoughts.todos.domain.TodoItemId;
import pl.devthoughts.todos.modules.protobuf.TodoItemRequestProtobufParser;
import pl.devthoughts.todos.service.TodoService;
import pl.devthougths.todos.ProtobufTodoItem;

import javax.inject.Inject;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import static pl.devthoughts.todos.modules.protobuf.TodoItemRequestProtobufParser.PROTOBUF_MIME_TYPE;

public class ProtobufTodoController extends Controller {

    private final TodoService todoService;

    @Inject
    public ProtobufTodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @BodyParser.Of(TodoItemRequestProtobufParser.class)
    public Result addItem() {
        TodoItemRequest request = getRequest();
        return todoService.saveItem(request)
            .map(itemId -> created(
                asProtobufContent(itemId)).withHeader("Content-Type", PROTOBUF_MIME_TYPE))
            .getOrElse(internalServerError());
    }

    private byte[] asProtobufContent(TodoItemId itemId) {
        return ProtobufTodoItem.CreateItemResponse.newBuilder().setId(itemId.getId()).build()
            .toByteArray();
    }

    private TodoItemRequest getRequest() {
        return request().body().as(TodoItemRequest.class);
    }

}
