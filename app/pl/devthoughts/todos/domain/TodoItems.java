package pl.devthoughts.todos.domain;

import java.util.Collection;
import java.util.Collections;

public class TodoItems {

    public static final TodoItems EMPTY = new TodoItems(Collections.EMPTY_LIST);

    private final Collection<TodoItem> items;

    public static TodoItems wrap(Collection<TodoItem> items) {
        return items.isEmpty() ? EMPTY : new TodoItems(items);
    }

    public TodoItems(Collection<TodoItem> items) {
        this.items = items;
    }

    public Collection<TodoItem> getItems() {
        return items;
    }
}
