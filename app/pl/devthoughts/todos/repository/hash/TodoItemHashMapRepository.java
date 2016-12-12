package pl.devthoughts.todos.repository.hash;

import javaslang.control.Option;
import pl.devthoughts.todos.domain.TodoItem;
import pl.devthoughts.todos.domain.TodoItemId;
import pl.devthoughts.todos.domain.TodoItems;
import pl.devthoughts.todos.repository.TodoItemRepository;

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
    public Option<TodoItem> findItem(TodoItemId id) {
        return Option.of(map.get(id.getId()));
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