package pl.devthoughts.todos.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import pl.devthoughts.todos.repository.TodoItemHashMapRepository;
import pl.devthoughts.todos.repository.TodoItemRepository;

class TodosModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(TodoItemRepository.class)
            .annotatedWith(Inject.class)
            .to(TodoItemHashMapRepository.class);
    }
}
