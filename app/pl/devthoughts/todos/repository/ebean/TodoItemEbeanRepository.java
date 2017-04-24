package pl.devthoughts.todos.repository.ebean;

import javaslang.control.Option;
import javaslang.control.Try;
import pl.devthoughts.todos.domain.TodoItem;
import pl.devthoughts.todos.domain.TodoItemId;
import pl.devthoughts.todos.repository.TodoItemRepository;
import play.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import static java.util.stream.Collectors.toList;
import static javaslang.API.$;
import static javaslang.API.Case;
import static javaslang.API.Match;
import static javaslang.Patterns.Failure;
import static javaslang.Patterns.None;
import static javaslang.Patterns.Some;
import static javaslang.Patterns.Success;

public class TodoItemEbeanRepository implements TodoItemRepository {

    private static final Logger.ALogger LOGGER = Logger.of(TodoItemEbeanRepository.class);

    @Override
    public Try<TodoItemId> saveItem(TodoItem item) {
        return Try.of(() -> Todo.from(item))
            .peek(todo -> todo.insert())
            .map(todo -> new TodoItemId(todo.getUuid()));
    }

    @Override
    public Try<TodoItem> findItem(TodoItemId itemId) {
        return find(() -> completeTodo(itemId.getId()))
            .map(item -> item.asDomainItem());
    }

    @Override
    public Try<TodoItem> updateItem(TodoItemId itemId, String name, Date dueDate) {
        return Try.of(() -> doesExist(itemId))
            .map(b -> b ?
                Try.success(b) :
                Try.failure(new IllegalStateException("Todo with id " + itemId.getId() + " does not exist")))
            .map(b -> new Todo(itemId.getId(), name, dueDate))
            .peek(todo -> todo.update())
            .map(todo -> todo.asDomainItem());
    }

    @Override
    public Try<TodoItem> removeItem(TodoItemId itemId) {
        return find(() -> completeTodo(itemId.getId()))
            .peek(todo -> todo.delete())
            .map(todo -> todo.asDomainItem());
    }

    @Override
    public Collection<TodoItem> findAllItems() {
        return Try.of(() -> Todo.find.findList())
            .map(todos -> todos.stream().map(item -> item.asDomainItem()).collect(toList()))
            .onFailure(ex -> LOGGER.error("Cannot fetch todo items", ex))
            .onSuccess(items -> LOGGER.info("Number of todo item found: {}", items.size()))
            .getOrElse(Collections.EMPTY_LIST);
    }

    @Override
    public Try<TodoItem> finishItem(TodoItemId itemId) {
        return find(() -> todoWithStatusOnly(itemId.getId()))
            .peek(todo -> todo.done())
            .map(todo -> todo.asDomainItem());
    }

    @Override
    public Try<TodoItem> reopenItem(TodoItemId itemId) {
        return find(() -> todoWithStatusOnly(itemId.getId()))
            .peek(todo -> todo.reopen())
            .map(todo -> todo.asDomainItem());
    }

    private boolean doesExist(TodoItemId itemId) {
        return countByUuid(itemId.getId()) > 0;
    }

    private int countByUuid(String uuid) {
        return Todo.find.where().eq("uuid", uuid).findRowCount();
    }

    private Try<Todo> find(Try.CheckedSupplier<Option<Todo>> todoSupplier) {
        return Try.of(todoSupplier)
            .transform(findResult -> Match(findResult).of(
                Case(Success(Some($())), t -> t.toTry()),
                Case(Success(None()), () -> Try.failure(
                    new IllegalArgumentException("Unknown todo item id provided []"))
                ),
                Case(Failure($()), e -> Try.failure(e))
                )
            );
    }

    private Option<Todo> completeTodo(String id) {
        return Option.of(Todo.find
            .where()
            .eq("uuid", id)
            .findUnique());
    }

    private Option<Todo> todoWithStatusOnly(String id) {
        return Option.of(Todo.find
            .select("status")
            .where()
            .eq("uuid", id)
            .findUnique());
    }

}
