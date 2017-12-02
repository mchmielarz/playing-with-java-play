package pl.devthoughts.todos.opencsv;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

import pl.devthoughts.todos.controllers.TodoItemRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeConverter extends AbstractBeanField<LocalDateTime> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TodoItemRequest.DUE_DATE_FORMAT);

    @Override
    protected Object convert(String value)
        throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        return LocalDateTime.parse(value, formatter);
    }

    @Override
    protected String convertToWrite(Object value) throws CsvDataTypeMismatchException {
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(formatter);
        } else {
            throw new CsvDataTypeMismatchException(value + " is not an instance of LocalDateTime");
        }
    }

}
