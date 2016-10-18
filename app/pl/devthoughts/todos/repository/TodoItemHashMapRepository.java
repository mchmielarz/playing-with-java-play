package pl.devthoughts.todos.repository;

import javaslang.control.Option;
import pl.devthoughts.todos.controllers.TodoItemRequest;
import pl.devthoughts.todos.domain.TodoItem;
import pl.devthoughts.todos.domain.TodoItemId;
import pl.devthoughts.todos.domain.TodoItems;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static javaslang.control.Option.some;

public class TodoItemHashMapRepository implements TodoItemRepository {

    private final Map<String, TodoItem> map;

    public TodoItemHashMapRepository() {
        map = newHashMap();
    }

    @Override
    public Option<TodoItemId> saveItem(TodoItem item) {
        map.put(item.getId(), item);
        return some(new TodoItemId(item.getId()));
    }

    @Override
    public Option<TodoItem> findItem(String id) {
        return Option.of(map.get(id));
    }

    @Override
    public void updateItem(TodoItem item, TodoItemRequest req) {
        TodoItem updatedItem = item.updateWith(req);
        map.put(updatedItem.getId(), updatedItem);
    }

    @Override
    public void removeItem(TodoItem item) {
        map.remove(item.getId());
    }

    @Override
    public TodoItems findAllItems() {
        return new TodoItems(map.values());
    }

    @Override
    public void finishItem(TodoItem it) {
        it.done();
    }

    @Override
    public void reopenItem(TodoItem it) {
        it.reopen();
    }
}