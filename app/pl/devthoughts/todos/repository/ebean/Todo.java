package pl.devthoughts.todos.repository.ebean;

import io.ebean.Finder;
import io.ebean.Model;

import pl.devthoughts.todos.TodosConfig;
import pl.devthoughts.todos.domain.TodoItem;
import pl.devthoughts.todos.domain.TodoItemStatus;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import play.data.format.Formats;
import play.data.validation.Constraints;

import static javax.persistence.EnumType.STRING;

@Entity
@Table(name = "TODOS")
public class Todo extends Model {

    @Id
    @Constraints.Required
    @Column(name = "item_id")
    private String uuid;

    @Constraints.Required
    @Column(name = "item_name")
    private String name;

    @Enumerated(STRING)
    private TodoItemStatus status;

    @Formats.DateTime(pattern = TodosConfig.DUE_DATE_FORMAT)
    private LocalDateTime dueDate;

    public Todo(String uuid, String name, LocalDateTime dueDate) {
        this.uuid = uuid;
        this.name = name;
        this.dueDate = dueDate;
    }

    public Todo(String uuid, String name, LocalDateTime dueDate, TodoItemStatus status) {
        this(uuid, name, dueDate);
        this.status = status;
    }

    String getUuid() {
        return uuid;
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

    public void done() {
        this.status = TodoItemStatus.DONE;
        save();
    }

    public void reopen() {
        this.status = TodoItemStatus.OPEN;
        save();
    }

    TodoItem asDomainItem() {
        return new TodoItem(getUuid(), getName(), getDueDate(), getStatus());
    }

    static final Finder<Long, Todo> find = new TodoFinder();

    public static Todo from(TodoItem item) {
        return new Todo(item.getId(), item.getName(), item.getDueDate(), item.getStatus());
    }
}
