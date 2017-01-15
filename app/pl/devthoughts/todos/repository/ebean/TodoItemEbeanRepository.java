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
            .peek(todo -> todo.save())
            .map(todo -> new TodoItemId(todo.getUuid()));
    }

    @Override
    public Try<TodoItem> findItem(TodoItemId itemId) {
        return find(itemId.getId())
            .map(item -> item.asDomainItem());
    }

    @Override
    public Try<TodoItem> updateItem(String id, String name, Date dueDate) {
        return find(id)
            .map(todo -> todo.withName(name))
            .map(todo -> todo.withDueDate(dueDate))
            .peek(todo -> todo.save())
            .map(todo -> todo.asDomainItem());
    }

    @Override
    public Try<TodoItem> removeItem(String itemId) {
        return find(itemId)
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
        return find(itemId.getId())
            .peek(todo -> todo.done())
            .map(todo -> todo.asDomainItem());
    }

    @Override
    public Try<TodoItem> reopenItem(TodoItemId itemId) {
        return find(itemId.getId())
            .peek(todo -> todo.reopen())
            .map(todo -> todo.asDomainItem());
    }

    private Try<Todo> find(String id) {
        return Try.of(
            () -> findSingleTodo(id)
        )
            .transform(findResult -> Match(findResult).of(
                Case(Success(Some($())), t -> t.toTry()),
                Case(Success(None()), () -> Try.failure(
                    new IllegalArgumentException("Unknown todo item id provided [" + id + "]"))
                ),
                Case(Failure($()), e -> Try.failure(e))
                )
            );
    }

    private Option<Todo> findSingleTodo(String id) {
        return Option.of(Todo.find
            .where()
            .eq("uuid", id)
            .findUnique());
    }

}
