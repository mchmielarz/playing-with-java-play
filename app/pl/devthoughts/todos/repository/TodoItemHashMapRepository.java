package pl.devthoughts.todos.repository;

import javaslang.control.Option;
import pl.devthoughts.todos.controllers.TodoItemRequest;
import pl.devthoughts.todos.domain.TodoItem;
import pl.devthoughts.todos.domain.TodoItemId;
import pl.devthoughts.todos.domain.TodoItems;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class TodoItemHashMapRepository {

    private final Map<String, TodoItem> map;

    public TodoItemHashMapRepository() {
        map = newHashMap();
    }

    public TodoItemId saveItem(TodoItem item) {
        map.put(item.getId(), item);
        return new TodoItemId(item.getId());
    }

    public Option<TodoItem> findItem(String id) {
        return Option.of(map.get(id));
    }

    public void updateItem(TodoItem item, TodoItemRequest req) {
        TodoItem updatedItem = item.updateWith(req);
        map.put(updatedItem.getId(), updatedItem);
    }

    public void removeItem(TodoItem item) {
        map.remove(item.getId());
    }

    public TodoItems findAllItems() {
        return new TodoItems(map.values());
    }

    public void finishItem(TodoItem it) {
        it.done();
    }

    public void reopenItem(TodoItem it) {
        it.reopen();
    }
}