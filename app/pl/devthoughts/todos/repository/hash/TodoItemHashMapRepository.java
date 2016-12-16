package pl.devthoughts.todos.repository.hash;

import javaslang.control.Option;
import javaslang.control.Try;
import pl.devthoughts.todos.domain.TodoItem;
import pl.devthoughts.todos.domain.TodoItemId;
import pl.devthoughts.todos.repository.TodoItemRepository;

import java.util.Collection;
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
        return Option.of(map.get(id.getId())).toTry();
    }

    @Override
    public void updateItem(TodoItem item) {
        map.put(item.getId(), item);
    }

    @Override
    public void removeItem(TodoItem item) {
        map.remove(item.getId());
    }

    @Override
    public Collection<TodoItem> findAllItems() {
        return map.values();
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