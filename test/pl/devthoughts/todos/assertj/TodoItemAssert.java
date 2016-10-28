package pl.devthoughts.todos.assertj;

import org.assertj.core.api.AbstractAssert;
import pl.devthoughts.todos.domain.TodoItem;

import java.util.Date;
import java.util.Objects;

public class TodoItemAssert extends AbstractAssert<TodoItemAssert, TodoItem> {

    public TodoItemAssert(TodoItem actual) {
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
        if (!Objects.equals(actual.getDueDate(), dueDate)) {
            failWithMessage("Expected item's dueDate to be <%s> but was <%s>", dueDate, actual.getDueDate());
        }
        return this;
    }
}
