package pl.devthoughts.todos.modules;

import com.google.inject.AbstractModule;
import pl.devthoughts.todos.repository.TodoItemHashMapRepository;
import pl.devthoughts.todos.repository.TodoItemRepository;

public class TodosModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(TodoItemRepository.class)
            .to(TodoItemHashMapRepository.class);
    }
}
