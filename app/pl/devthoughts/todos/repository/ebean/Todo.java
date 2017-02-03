package pl.devthoughts.todos.repository.ebean;

import com.avaje.ebean.Model;
import pl.devthoughts.todos.domain.TodoItem;
import pl.devthoughts.todos.domain.TodoItemStatus;
import play.data.format.Formats;
import play.data.validation.Constraints;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

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

    @Formats.DateTime(pattern = "yyyy-MM-dd'T'HH:mm")
    private Date dueDate;

    public Todo(String uuid, String name, Date dueDate, TodoItemStatus status) {
        this.uuid = uuid;
        this.name = name;
        this.dueDate = dueDate;
        this.status = status;
    }

    public String getUuid() {
        return uuid;
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

    public Todo withName(String name) {
        this.name = name;
        return this;
    }

    public Todo withDueDate(Date dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    public void done() {
        this.status = TodoItemStatus.DONE;
        save();
    }

    public void reopen() {
        this.status = TodoItemStatus.OPEN;
        save();
    }

    public TodoItem asDomainItem() {
        return new TodoItem(getUuid(), getName(), getDueDate(), getStatus());
    }

    public static Finder<Long, Todo> find = new Finder<>(Todo.class);

    public static Todo from(TodoItem item) {
        return new Todo(item.getId(), item.getName(), item.getDueDate(), item.getStatus());
    }
}
