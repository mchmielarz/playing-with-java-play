package pl.devthoughts.todos.controllers;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;

import java.util.Date;

public class TodoItemRequest {

    @CsvBindByPosition(position = 0)
    private String name;
    @CsvBindByPosition(position = 1)
    @CsvDate(value = "yyyy-MM-dd HH:mm")
    @JsonFormat
        (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private Date dueDate;

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
