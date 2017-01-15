package pl.devthoughts.todos.repository;

import com.google.common.collect.ImmutableMap;
import javaslang.control.Option;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pl.devthoughts.todos.domain.TodoItem;
import pl.devthoughts.todos.domain.TodoItemId;
import pl.devthoughts.todos.repository.sql.TodoItemSqlRepository;
import play.db.Database;
import play.db.Databases;
import play.db.evolutions.Evolutions;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.devthoughts.todos.assertj.TodoItemAssert.assertThat;

public class TodoItemSqlRepositoryTest {

    private TodoItemSqlRepository repository;
    private Database db;

    @Before
    public void setup() {
        db = Databases.inMemory("todos", "jdbc:h2:mem:todos;MODE=PostgreSQL;DATABASE_TO_UPPER=false", ImmutableMap.of());
        Evolutions.applyEvolutions(db);
        repository = new TodoItemSqlRepository(db);
    }

    @After
    public void cleanup() {
        Evolutions.cleanupEvolutions(db);
        db.shutdown();
    }

    @Test
    public void should_save_a_single_todo() {
        final Date dueDate = new Date();
        final String name = "Do something";

        final Option<TodoItemId> itemId = repository.saveItem(new TodoItem(name, dueDate)).getOption();
        assertThat(itemId.isDefined()).isEqualTo(true);

        final Option<TodoItem> item = repository.findItem(itemId.get()).getOption();
        assertThat(item.isDefined());
        assertThat(item.get())
            .hasName(name)
            .hasDueDate(dueDate);
    }

}