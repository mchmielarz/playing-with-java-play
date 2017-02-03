package pl.devthoughts.todos.service;

import com.google.inject.Inject;
import javaslang.control.Option;
import javaslang.control.Try;
import org.jetbrains.annotations.NotNull;
import pl.devthoughts.todos.controllers.TodoItemRequest;
import pl.devthoughts.todos.domain.TodoItem;
import pl.devthoughts.todos.domain.TodoItemId;
import pl.devthoughts.todos.domain.TodoItems;
import pl.devthoughts.todos.repository.TodoItemRepository;
import play.Logger;

import static pl.devthoughts.todos.domain.TodoItem.from;

public class TodoService {

    private static final Logger.ALogger LOGGER = Logger.of(TodoService.class);

    private final TodoItemRepository repository;

    @Inject
    public TodoService(TodoItemRepository repository) {
        this.repository = repository;
    }

    public Option<TodoItemId> saveItem(TodoItemRequest request) {
        return repository.saveItem(from(request))
            .onFailure(ex -> LOGGER.error("Cannot save a new todo item: {}", request, ex))
            .onSuccess(item -> LOGGER.info("New todo item stored: {}", item.getId()))
            .toOption();
    }

    public Option<TodoItem> findItem(String id) {
        return repository.findItem(wrapped(id))
            .onFailure(ex -> LOGGER.error("Cannot find an item with id: {}", id, ex))
            .toOption();
    }

    public Try<TodoItem> updateItem(String id, TodoItemRequest request) {
        return repository.updateItem(wrapped(id), request.getName(), request.getDueDate())
            .onSuccess(item -> LOGGER.info("Item [{}] updated successfully.", id))
            .onFailure(ex -> LOGGER.error("Item [{}] not found.", id, ex));
    }

    public Try<TodoItem> deleteItem(String id) {
        return repository.removeItem(wrapped(id))
            .onFailure(ex -> LOGGER.error("Cannot remove existing item [{}]", id, ex))
            .onSuccess(item -> LOGGER.info("Item [{}] has been removed.", item.getId()));
    }

    public Try<TodoItem> done(String id) {
        return repository.finishItem(wrapped(id))
            .onFailure(ex -> LOGGER.error("Cannot finish item: {}", id, ex))
            .onSuccess(todo -> LOGGER.info("Todo item [{}] has been finished.", id));
    }

    public Try<TodoItem> reopen(String id) {
        return repository.reopenItem(wrapped(id))
            .onFailure(ex -> LOGGER.error("Cannot reopen item: {}", id, ex))
            .onSuccess(todo -> LOGGER.info("Todo item [{}] has been reopened.", id));
    }

    public TodoItems getAllItems() {
        return TodoItems.wrap(repository.findAllItems());
    }

    @NotNull
    private TodoItemId wrapped(String id) {
        return new TodoItemId(id);
    }

}
