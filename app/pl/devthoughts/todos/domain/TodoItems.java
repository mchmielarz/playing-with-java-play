package pl.devthoughts.todos.domain;

import java.util.Collection;

public class TodoItems {

    private final Collection<TodoItem> items;

    public TodoItems(Collection<TodoItem> items) {
        this.items = items;
    }

    public Collection<TodoItem> getItems() {
        return items;
    }
}
