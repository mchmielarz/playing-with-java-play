package pl.devthoughts.todos.domain;

import java.util.Collection;

import static java.util.Collections.EMPTY_LIST;

public class TodoItems {

    private static final TodoItems EMPTY = new TodoItems(EMPTY_LIST);

    private final Collection<TodoItem> items;

    public static TodoItems wrap(Collection<TodoItem> items) {
        return items.isEmpty() ? EMPTY : new TodoItems(items);
    }

    private TodoItems(Collection<TodoItem> items) {
        this.items = items;
    }

    public Collection<TodoItem> getItems() {
        return items;
    }
}
