package pl.devthoughts.todos.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import pl.devthoughts.todos.TodosConfig;
import pl.devthoughts.todos.controllers.TodoItemRequest;

import java.time.LocalDateTime;
import java.util.UUID;

import static pl.devthoughts.todos.domain.TodoItemStatus.DONE;
import static pl.devthoughts.todos.domain.TodoItemStatus.OPEN;

public class TodoItem {

    private String id;
    private String name;
    private TodoItemStatus status;
    @JsonFormat
        (shape = JsonFormat.Shape.STRING, pattern = TodosConfig.DUE_DATE_FORMAT)
    private LocalDateTime dueDate;

    static public TodoItem from(TodoItemRequest req) {
        return new TodoItem(req.getName(), req.getDueDate());
    }

    public TodoItem() {
        this.id = UUID.randomUUID().toString();
    }

    public TodoItem(String name, LocalDateTime dueDate) {
        this();
        this.name = name;
        this.dueDate = dueDate;
        this.status = OPEN;
    }

    public TodoItem(String id, String name, LocalDateTime dueDate, TodoItemStatus status) {
        this.id = id;
        this.name = name;
        this.dueDate = dueDate;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public TodoItemStatus getStatus() {
        return status;
    }

    public TodoItem withName(String name) {
        this.name = name;
        return this;
    }

    public TodoItem withDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    public void done() {
        this.status = DONE;
    }

    public void reopen() {
        this.status = OPEN;
    }

    public boolean isOpened() {
        return OPEN == status;
    }

    public boolean isClosed() {
        return DONE == status;
    }
}
