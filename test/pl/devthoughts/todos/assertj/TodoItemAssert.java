package pl.devthoughts.todos.assertj;

import org.assertj.core.api.AbstractAssert;
import pl.devthoughts.todos.domain.TodoItem;

import java.util.Date;
import java.util.Objects;

import static pl.devthoughts.todos.repository.sql.TodoItemSqlRepository.DATE_FORMAT;

public class TodoItemAssert extends AbstractAssert<TodoItemAssert, TodoItem> {

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

    public TodoItemAssert hasDueDate(Date dueDate) {
        isNotNull();
        final String expected = DATE_FORMAT.format(dueDate);
        final String current = DATE_FORMAT.format(this.actual.getDueDate());
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
