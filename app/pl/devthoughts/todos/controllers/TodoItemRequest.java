package pl.devthoughts.todos.controllers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;

import pl.devthoughts.todos.TodosConfig;
import pl.devthoughts.todos.opencsv.LocalDateTimeConverter;

import java.time.LocalDateTime;

public class TodoItemRequest {

    @CsvBindByName(column = "name", required = true)
    @JsonProperty(required = true)
    private String name;

    @CsvCustomBindByName(column = "dueDate", required = true, converter = LocalDateTimeConverter.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TodosConfig.DUE_DATE_FORMAT)
    @JsonProperty(required = true)
    private LocalDateTime dueDate;

    public TodoItemRequest() {}

    @JsonCreator
    public TodoItemRequest(
        @JsonProperty("name") String name,
        @JsonProperty("dueDate") LocalDateTime dueDate
    ) {
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

    @Override public String toString() {
        return "TodoItemRequest{" +
               "name='" + name + '\'' +
               ", dueDate=" + dueDate +
               '}';
    }
}
