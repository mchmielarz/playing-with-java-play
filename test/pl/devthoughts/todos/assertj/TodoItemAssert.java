package pl.devthoughts.todos.assertj;

import org.assertj.core.api.AbstractAssert;

import pl.devthoughts.todos.domain.TodoItem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class TodoItemAssert extends AbstractAssert<TodoItemAssert, TodoItem> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    TodoItemAssert(TodoItem actual) {
        super(actual, TodoItemAssert.class);
    }

    public static TodoItemAssert assertThat(TodoItem actual) {
        return new TodoItemAssert(actual);
    }

    public TodoItemAssert hasName(String name) {
        isNotNull();
        if (!Objects.equals(actual.getName(), name)) {
            failWithMessage("Expected item's name to be <%s> but was <%s>", name, actual.getName());
        }
        return this;
    }

    public TodoItemAssert hasDueDate(LocalDateTime dueDate) {
        isNotNull();
        final String expected = dueDate.format(FORMATTER);
        final String current = this.actual.getDueDate().format(FORMATTER);
        if (!Objects.equals(expected, current)) {
            failWithMessage("Expected item's dueDate to be <%s> but was <%s>", dueDate, this.actual.getDueDate());
        }
        return this;
    }

    public TodoItemAssert isOpened() {
        isNotNull();
        if (!actual.isOpened()) {
            failWithMessage("Expected item to be opened but was closed");
        }

        return this;
    }

    public TodoItemAssert isClosed() {
        isNotNull();
        if (!actual.isClosed()) {
            failWithMessage("Expected item to be closed but was opened");
        }

        return this;
    }
}
