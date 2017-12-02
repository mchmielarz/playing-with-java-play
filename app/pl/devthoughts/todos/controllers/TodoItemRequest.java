package pl.devthoughts.todos.controllers;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;

import pl.devthoughts.todos.TodosConfig;
import pl.devthoughts.todos.opencsv.LocalDateTimeConverter;

import java.time.LocalDateTime;

public class TodoItemRequest {

    @CsvBindByName(column = "name", required = true)
    private String name;

    @CsvCustomBindByName(column = "dueDate", required = true, converter = LocalDateTimeConverter.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TodosConfig.DUE_DATE_FORMAT)
    private LocalDateTime dueDate;

    public TodoItemRequest() {}

    public TodoItemRequest(String name, LocalDateTime dueDate) {
        this();
        this.name = name;
        this.dueDate = dueDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
}
