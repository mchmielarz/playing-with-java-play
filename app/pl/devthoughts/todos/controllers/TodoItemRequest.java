package pl.devthoughts.todos.controllers;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;

import pl.devthoughts.todos.opencsv.LocalDateTimeConverter;

import java.time.LocalDateTime;

public class TodoItemRequest {

    public static final String DUE_DATE_FORMAT = "yyyy-MM-dd HH:mm";

    @CsvBindByName(column = "name", required = true)
    private String name;

    @CsvCustomBindByName(column = "dueDate", required = true, converter = LocalDateTimeConverter.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DUE_DATE_FORMAT)
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
