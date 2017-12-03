package pl.devthoughts.todos.repository.sql;

import io.vavr.control.Try;

import pl.devthoughts.todos.domain.TodoItem;
import pl.devthoughts.todos.domain.TodoItemId;
import pl.devthoughts.todos.domain.TodoItemStatus;
import pl.devthoughts.todos.repository.TodoItemRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;

import javax.inject.Inject;

import play.db.Database;
import play.db.NamedDatabase;

import static pl.devthoughts.todos.TimeUtils.asLocalDateTime;
import static pl.devthoughts.todos.TimeUtils.asTimestamp;

public class TodoItemSqlRepository implements TodoItemRepository {

    private static final String INSERT_TODO_ITEM =
        "INSERT INTO todos (item_id, item_name, status, due_date) VALUES(?, ?, ?, ?)";
    private static final String FIND_BY_ID =
        "SELECT * FROM todos WHERE item_id = ?";

    private final Database db;

    @Inject
    public TodoItemSqlRepository(@NamedDatabase("todos") Database db) {
        this.db = db;
    }

    public Try<TodoItemId> saveItem(TodoItem item) {
        return Try.of(() -> insertNewItem(item))
            .map(id -> new TodoItemId(id));
    }

    public Try<TodoItem> findItem(TodoItemId id) {
        return Try.of(() -> fetchItem(id.getId()));
    }

    private String insertNewItem(TodoItem item) throws SQLException {
        try(Connection conn = db.getConnection()) {
            try (final PreparedStatement stmt = conn.prepareStatement(INSERT_TODO_ITEM)) {
                stmt.setString(1, item.getId());
                stmt.setString(2, item.getName());
                stmt.setString(3, item.getStatus().name());
                stmt.setTimestamp(4, asTimestamp(item.getDueDate()));
                stmt.executeUpdate();
            }
        }
        return item.getId();
    }

    private TodoItem fetchItem(String id) throws SQLException {
        TodoItem item;
        try (Connection conn = db.getConnection()) {
            try (final PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID)) {
                stmt.setString(1, id);
                try (final ResultSet result = stmt.executeQuery()) {
                    result.next();
                    String status = result.getString("status");
                    String name = result.getString("item_name");
                    Timestamp dueDate = result.getTimestamp("due_date");
                    item = new TodoItem(id, name, asLocalDateTime(dueDate), TodoItemStatus.valueOf(status));
                }
            }
        }
        return item;
    }

    @Override
    public Try<TodoItem> updateItem(TodoItemId itemId, String name, LocalDateTime dueDate) {
        throw new UnsupportedOperationException("Guess what? It's not implemented!");
    }

    @Override
    public Try<TodoItem> removeItem(TodoItemId itemId) {
        throw new UnsupportedOperationException("Guess what? It's not implemented!");
    }

    @Override
    public void removeAll() {
        throw new UnsupportedOperationException("Guess what? It's not implemented!");
    }

    @Override
    public Collection<TodoItem> findAllItems() {
        throw new UnsupportedOperationException("Guess what? It's not implemented!");
    }

    @Override
    public Try<TodoItem> finishItem(TodoItemId itemId) {
        throw new UnsupportedOperationException("Guess what? It's not implemented!");
    }

    @Override
    public Try<TodoItem> reopenItem(TodoItemId itemId) {
        throw new UnsupportedOperationException("Guess what? It's not implemented!");
    }
}
