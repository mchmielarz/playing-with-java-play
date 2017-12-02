package pl.devthoughts.todos.repository;

import io.vavr.control.Try;

import pl.devthoughts.todos.domain.TodoItem;
import pl.devthoughts.todos.domain.TodoItemId;

import java.time.LocalDateTime;
import java.util.Collection;

public interface TodoItemRepository {

    Try<TodoItemId> saveItem(TodoItem item);

    Try<TodoItem> findItem(TodoItemId id);

    Try<TodoItem> updateItem(TodoItemId itemId, String name, LocalDateTime dueDate);

    Try<TodoItem> removeItem(TodoItemId itemId);

    Collection<TodoItem> findAllItems();

    Try<TodoItem> finishItem(TodoItemId itemId);

    Try<TodoItem> reopenItem(TodoItemId itemId);

    void removeAll();
}
