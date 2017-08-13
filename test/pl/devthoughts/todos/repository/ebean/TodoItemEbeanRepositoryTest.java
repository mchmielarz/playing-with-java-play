package pl.devthoughts.todos.repository.ebean;

import io.ebean.Ebean;
import io.vavr.control.Try;

import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Test;

import pl.devthoughts.todos.domain.TodoItem;
import pl.devthoughts.todos.domain.TodoItemId;

import java.util.Collection;
import java.util.Date;

import play.Application;
import play.test.WithApplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static pl.devthoughts.todos.assertj.TodoItemAssert.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;

public class TodoItemEbeanRepositoryTest extends WithApplication {

    private static final String DO_SOMETHING = "do something";
    private static final String PRINT_DOC = "print doc";
    private static final String DRAW_SKETCH = "draw sketch";

    private TodoItemEbeanRepository repository = new TodoItemEbeanRepository();

    @After
    public void cleanup() {
        Ebean.createSqlUpdate("DELETE FROM todos WHERE 1=1").execute();
    }

    @Test
    public void should_save_a_single_todo() {
        final TodoItemId id = storeSingleItem(DO_SOMETHING);

        final Try<TodoItem> item = repository.findItem(id);

        assertTrue(item.isSuccess());
        assertThat(item.get()).hasName(DO_SOMETHING);
    }

    @Test
    public void should_find_all_todos_stored() {
        storeSingleItem(DO_SOMETHING);
        storeSingleItem(PRINT_DOC);
        storeSingleItem(DRAW_SKETCH);

        final Collection<TodoItem> items = repository.findAllItems();

        assertThat(items)
                .extracting("name")
                .containsOnly(DO_SOMETHING, PRINT_DOC, DRAW_SKETCH);
    }

    @Test
    public void should_close_given_todo() {
        final TodoItemId itemId = storeSingleItem(DO_SOMETHING);

        final Try<TodoItem> item = repository.finishItem(itemId);

        assertTrue(item.isSuccess());
        assertThat(item.get()).isClosed();
    }

    @Test
    public void should_open_given_todo() {
        final TodoItemId itemId = storeSingleItem(DO_SOMETHING);

        final Try<TodoItem> item = repository.reopenItem(itemId);

        assertTrue(item.isSuccess());
        assertThat(item.get()).isOpened();
    }

    @Test
    public void should_remove_given_todo() {
        final TodoItemId itemId = storeSingleItem(DO_SOMETHING);

        final Try<TodoItem> item = repository.removeItem(itemId);

        assertTrue(item.isSuccess());
        assertTrue(repository.findItem(itemId).isFailure());
    }

    @Test
    public void should_update_given_todo() {
        final TodoItemId itemId = storeSingleItem(DO_SOMETHING);

        final Date newDate = new Date();
        final String newName = "something else";
        final Try<TodoItem> item = repository.updateItem(itemId, newName, newDate);

        assertTrue(item.isSuccess());
        assertThat(item.get()).hasName(newName);
        assertThat(item.get()).hasDueDate(newDate);
    }

    @NotNull
    private TodoItemId storeSingleItem(String itemName) {
        final Try<TodoItemId> id = repository.saveItem(new TodoItem(itemName, new Date()));
        assertTrue(id.isSuccess());
        return id.get();
    }

    @Override
    protected Application provideApplication() {
        return fakeApplication(inMemoryDatabase());
    }

}