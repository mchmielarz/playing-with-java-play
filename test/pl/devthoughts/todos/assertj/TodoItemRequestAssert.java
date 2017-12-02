package pl.devthoughts.todos.assertj;

import org.assertj.core.api.AbstractAssert;

import pl.devthoughts.todos.controllers.TodoItemRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class TodoItemRequestAssert extends AbstractAssert<TodoItemRequestAssert, TodoItemRequest> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    TodoItemRequestAssert(TodoItemRequest actual) {
        super(actual, TodoItemRequestAssert.class);
    }

    public static TodoItemRequestAssert assertThat(TodoItemRequest actual) {
        return new TodoItemRequestAssert(actual);
    }

    public TodoItemRequestAssert hasName(String name) {
        isNotNull();
        if (!Objects.equals(actual.getName(), name)) {
            failWithMessage("Expected item's name to be <%s> but was <%s>", name, actual.getName());
        }
        return this;
    }

    public TodoItemRequestAssert hasDueDate(LocalDateTime dueDate) {
        isNotNull();
        final String expected = dueDate.format(FORMATTER);
        final String current = this.actual.getDueDate().format(FORMATTER);
        if (!Objects.equals(expected, current)) {
            failWithMessage("Expected item's dueDate to be <%s> but was <%s>", dueDate, this.actual.getDueDate());
        }
        return this;
    }

}
