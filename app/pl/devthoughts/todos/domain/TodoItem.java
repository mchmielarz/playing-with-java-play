package pl.devthoughts.todos.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import pl.devthoughts.todos.controllers.TodoItemRequest;

import java.util.Date;
import java.util.UUID;

public class TodoItem {

    private String id;
    private String name;
    private TodoItemStatus status;
    @JsonFormat
        (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date dueDate;

    static public TodoItem fromRequest(TodoItemRequest req) {
        return new TodoItem(req.getName(), req.getDueDate());
    }

    public TodoItem() {
        this.id = UUID.randomUUID().toString();
    }

    public TodoItem(String name, Date dueDate) {
        this();
        this.name = name;
        this.dueDate = dueDate;
        this.status = TodoItemStatus.OPEN;
    }

    private TodoItem(String id, String name, Date dueDate, TodoItemStatus status) {
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

    public Date getDueDate() {
        return dueDate;
    }

    public TodoItemStatus getStatus() {
        return status;
    }

    public TodoItem updateWith(TodoItemRequest req) {
        return new TodoItem(this.id, req.getName(), req.getDueDate(), this.status);
    }

    public void done() {
        this.status = TodoItemStatus.DONE;
    }

    public void open() {
        this.status = TodoItemStatus.OPEN;
    }
}
