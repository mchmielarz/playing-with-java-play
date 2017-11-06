package pl.devthoughts.todos.assertj;

import org.assertj.core.api.AbstractAssert;

import pl.devthoughts.todos.controllers.TodoItemRequest;

import java.util.Date;
import java.util.Objects;

import static pl.devthoughts.todos.repository.sql.TodoItemSqlRepository.DATE_FORMAT;

public class TodoItemRequestAssert extends AbstractAssert<TodoItemRequestAssert, TodoItemRequest> {

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

    public TodoItemRequestAssert hasDueDate(Date dueDate) {
        isNotNull();
        final String expected = DATE_FORMAT.format(dueDate);
        final String current = DATE_FORMAT.format(this.actual.getDueDate());
        if (!Objects.equals(expected, current)) {
            failWithMessage("Expected item's dueDate to be <%s> but was <%s>", dueDate, this.actual.getDueDate());
        }
        return this;
    }

}
