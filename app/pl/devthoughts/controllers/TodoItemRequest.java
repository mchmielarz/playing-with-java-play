package pl.devthoughts.controllers;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class TodoItemRequest {

    private String name;
    @JsonFormat
        (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date dueDate;

    private TodoItemRequest() {
    }

    public TodoItemRequest(String name, Date dueDate) {
        this();
        this.name = name;
        this.dueDate = dueDate;
    }

    public String getName() {
        return name;
    }

    public Date getDueDate() {
        return dueDate;
    }
}
