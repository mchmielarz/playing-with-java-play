package pl.devthoughts.todos.repository;

import com.google.common.collect.ImmutableMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import pl.devthoughts.todos.domain.TodoItem;
import pl.devthoughts.todos.domain.TodoItemId;
import pl.devthoughts.todos.repository.sql.TodoItemSqlRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

import io.vavr.collection.List;
import io.vavr.control.Option;
import play.db.Database;
import play.db.Databases;
import play.db.evolutions.Evolutions;

import static java.util.stream.Collectors.toList;
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
    public void etwas() {
        Option.none()
            .onEmpty(() -> System.out.println(""))
            .orElse(() -> Option.none());

        // list of optional postal codes into optional addresses and take first one, not wrapped with optional type
        // list of optionals into list of values

        java.util.List<Optional<String>> strings = new ArrayList<>();
        strings.add(Optional.of("1"));
        strings.add(Optional.of("2"));
        // this
        strings.stream()
            .map(o -> findBy(o))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toList());
        // or this
        strings.stream()
            .map(o -> findBy(o))
            .flatMap(o -> o.isPresent() ? Stream.of(o.get()) : Stream.empty())
            .collect(toList());
        // after JDK9 release
        list.stream()
            .flatMap(Optional::stream)
            .collect(toList());

        final List<Option<String>> objects = List.of(Option.of("1"), Option.of("2"));
        final List<Integer> integers = objects
            .map(o -> o.map((String v) -> Integer.valueOf(v)))
            .flatMap(Option::toStream);
        integers
            .get();

        Optional.of("").flatMap(addr -> addr == null ? Optional.of("ew") : addr);

        List<Optional<String>> list = new ArrayList<>();
        list.add(Optional.of("etwas"));
        list.add(Optional.of("jemand"));
        list.add(Optional.of(""));
        list.add(Optional.ofNullable(null));
        System.out.println(list);
        System.out.println(list.stream()
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toList()));
    }

    private Optional<Integer> findBy(Optional<String> value) {
        if (value.isPresent()) {
            return Optional.of(Integer.valueOf(value.get()));
        } else {
            return Optional.empty();
        }
    }

    @Test
    public void should_save_a_single_todo() {
        final Date dueDate = new Date();
        final String name = "Do something";

        final Option<TodoItemId> itemId = repository.saveItem(new TodoItem(name, dueDate)).toOption();
        assertThat(itemId.isDefined()).isEqualTo(true);

        final Option<TodoItem> item = repository.findItem(itemId.get()).toOption();
        assertThat(item.isDefined());
        assertThat(item.get())
            .hasName(name)
            .hasDueDate(dueDate);
    }

}