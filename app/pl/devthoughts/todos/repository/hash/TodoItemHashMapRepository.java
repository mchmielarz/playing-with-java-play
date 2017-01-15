package pl.devthoughts.todos.repository.hash;

import javaslang.control.Option;
import javaslang.control.Try;
import pl.devthoughts.todos.domain.TodoItem;
import pl.devthoughts.todos.domain.TodoItemId;
import pl.devthoughts.todos.repository.TodoItemRepository;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class TodoItemHashMapRepository implements TodoItemRepository {

    private final Map<String, TodoItem> map;

    public TodoItemHashMapRepository() {
        map = newHashMap();
    }

    @Override
    public Try<TodoItemId> saveItem(TodoItem item) {
        map.put(item.getId(), item);
        return Try.success(new TodoItemId(item.getId()));
    }

    @Override
    public Try<TodoItem> findItem(TodoItemId id) {
        return foundItem(id.getId()).toTry();
    }

    @Override
    public Try<TodoItem> updateItem(String itemId, String name, Date dueDate) {
        return foundItem(itemId)
            .map(item -> item.withName(name))
            .map(item -> item.withDueDate(dueDate))
            .peek(item -> map.put(item.getId(), item))
            .toTry();
    }

    @Override
    public Try<TodoItem> removeItem(String itemId) {
        return Try.of(() -> map.remove(itemId));
    }

    @Override
    public Collection<TodoItem> findAllItems() {
        return map.values();
    }

    @Override
    public Try<TodoItem> finishItem(TodoItemId itemId) {
        return foundItem(itemId.getId())
            .peek(item -> item.done())
            .toTry();
    }

    @Override
    public Try<TodoItem> reopenItem(TodoItemId itemId) {
        return foundItem(itemId.getId())
            .peek(item -> item.reopen())
            .toTry();
    }

    private Option<TodoItem> foundItem(String itemId) {
        return Option.of(map.get(itemId));
    }
}