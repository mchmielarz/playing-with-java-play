package pl.devthoughts.todos.repository.ebean;

import io.ebean.Finder;

class TodoFinder extends Finder<Long, Todo> {

    TodoFinder() {
        super(Todo.class);
    }



}
