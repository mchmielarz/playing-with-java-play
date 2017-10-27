package pl.devthoughts.todos.controllers;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;

import java.util.Date;

public class TodoItemRequest {

    public static final String DUE_DATE_FORMAT = "yyyy-MM-dd HH:mm";

    @CsvBindByName(column = "name")
    private String name;

    @CsvBindByName(column = "dueDate")
    @CsvDate(value = DUE_DATE_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DUE_DATE_FORMAT)
    private Date dueDate;

    public TodoItemRequest() {}

    public TodoItemRequest(String name, Date dueDate) {
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

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
}
