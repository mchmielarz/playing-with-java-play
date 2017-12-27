package pl.devthoughts.todos.controllers.protobuf;

import io.vavr.collection.List;
import io.vavr.control.Option;
import pl.devthoughts.todos.controllers.TodoItemRequest;
import pl.devthoughts.todos.domain.TodoItem;
import pl.devthoughts.todos.domain.TodoItemId;
import pl.devthoughts.todos.modules.protobuf.ProtobufParser;
import pl.devthoughts.todos.service.TodoService;
import pl.devthougths.todos.ProtobufTodoItem;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

import static pl.devthoughts.todos.TimeUtils.asLocalDateTime;
import static pl.devthoughts.todos.TimeUtils.asProtobufTimestamp;
import static pl.devthoughts.todos.modules.protobuf.TodoItemRequestProtobufParser.PROTOBUF_MIME_TYPE;

public class ProtobufTodoController extends Controller {

    private final TodoService todoService;

    @Inject
    public ProtobufTodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @ProtobufParser(ProtobufTodoItem.CreateItemRequest.class)
    public Result addItem() {
        ProtobufTodoItem.CreateItemRequest request = getRequest(ProtobufTodoItem.CreateItemRequest.class);
        return todoService.saveItem(new TodoItemRequest(request.getName(), asLocalDateTime(request.getDueDate())))
            .map(itemId -> created(
                asProtobufContent(itemId)).withHeader("Content-Type", PROTOBUF_MIME_TYPE))
            .getOrElse(internalServerError());
    }

    @ProtobufParser(ProtobufTodoItem.FetchItemsRequest.class)
    public Result findItems() {
        ProtobufTodoItem.FetchItemsRequest request = getRequest(ProtobufTodoItem.FetchItemsRequest.class);
        final List<TodoItem> items =
            extractItemsIds(request)
            .map(todoService::findItem)
            .filter(Option::isDefined)
            .map(Option::get);
        return ok(asProtobufContent(items)).withHeader("Content-Type", PROTOBUF_MIME_TYPE);
    }

    private List<String> extractItemsIds(ProtobufTodoItem.FetchItemsRequest request) {
        List<String> itemsIds = List.empty();
        for (int idx = 0; idx < request.getIdCount(); idx++) {
            itemsIds = itemsIds.append(request.getId(idx));
        }
        return itemsIds;
    }

    private byte[] asProtobufContent(TodoItemId itemId) {
        return ProtobufTodoItem.CreateItemResponse.newBuilder().setId(itemId.getId()).build()
            .toByteArray();
    }

    private byte[] asProtobufContent(List<TodoItem> todoItems) {
        final List<ProtobufTodoItem.FetchItemsResponse.Item> items = todoItems.map(i -> ProtobufTodoItem.FetchItemsResponse.Item.newBuilder()
            .setId(i.getId())
            .setName(i.getName())
            .setDueDate(asProtobufTimestamp(i.getDueDate()))
            .setStatus(i.getStatus().name())
            .build());
        return ProtobufTodoItem.FetchItemsResponse.newBuilder().addAllItem(items).build()
            .toByteArray();
    }

    private <T> T getRequest(Class<T> klas) {
        return request().body().as(klas);
    }
}
