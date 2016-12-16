package pl.devthoughts.todos.repository;

import javaslang.control.Try;
import pl.devthoughts.todos.domain.TodoItem;
import pl.devthoughts.todos.domain.TodoItemId;

import java.util.Collection;

public interface TodoItemRepository {

    Try<TodoItemId> saveItem(TodoItem item);

    Try<TodoItem> findItem(TodoItemId id);

    void updateItem(TodoItem item);

    void removeItem(TodoItem item);

    Collection<TodoItem> findAllItems();

    void finishItem(TodoItem it);

    void reopenItem(TodoItem it);

}
