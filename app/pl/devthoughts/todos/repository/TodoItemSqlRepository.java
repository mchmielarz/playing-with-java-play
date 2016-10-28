package pl.devthoughts.todos.repository;

import javaslang.control.Option;
import javaslang.control.Try;
import pl.devthoughts.todos.controllers.TodoItemRequest;
import pl.devthoughts.todos.domain.TodoItem;
import pl.devthoughts.todos.domain.TodoItemId;
import pl.devthoughts.todos.domain.TodoItemStatus;
import pl.devthoughts.todos.domain.TodoItems;
import play.Logger;
import play.db.Database;
import play.db.NamedDatabase;

import javax.inject.Inject;
import java.sql.*;
import java.util.Calendar;
import java.util.Date;

public class TodoItemSqlRepository implements TodoItemRepository {

    private static final Logger.ALogger LOGGER = Logger.of(TodoItemSqlRepository.class);

    public static final String INSERT_TODO_ITEM =
        "INSERT INTO todos (item_id, item_name, status, due_date) VALUES(?, ?, ?, ?)";
    public static final String FIND_BY_ID =
        "SELECT * FROM todos WHERE item_id = ?";

    private final Database db;

    @Inject
    public TodoItemSqlRepository(@NamedDatabase("todos") Database db) {
        this.db = db;
    }

    public Option<TodoItemId> saveItem(TodoItem item) {
        return Try.of(() -> insertNewItem(item))
            .map(id -> new TodoItemId(id))
            .onSuccess(itemId -> LOGGER.info("New todo item stored: {} {}", itemId.getId(), item.getName()))
            .onFailure(ex -> LOGGER.error("Cannot store todo item: {}", item.getName(), ex))
            .getOption();
    }

    public Option<TodoItem> findItem(TodoItemId id) {
        return Try.of(() -> fetchItem(id.getId()))
            .onSuccess(item -> LOGGER.info("Todo item found: {}", item))
            .onFailure(ex -> LOGGER.error("Cannot find todo item with id: {}", id, ex))
            .getOption();
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
                    item = new TodoItem(id, name, asDate(dueDate), TodoItemStatus.valueOf(status));
                }
            }
        }
        return item;
    }

    @Override
    public void updateItem(TodoItem item, TodoItemRequest req) {
        throw new UnsupportedOperationException("Guess what? It's not implemented!");
    }

    @Override
    public void removeItem(TodoItem item) {
        throw new UnsupportedOperationException("Guess what? It's not implemented!");
    }

    @Override
    public TodoItems findAllItems() {
        throw new UnsupportedOperationException("Guess what? It's not implemented!");
    }

    @Override
    public void finishItem(TodoItem it) {
        throw new UnsupportedOperationException("Guess what? It's not implemented!");
    }

    @Override
    public void reopenItem(TodoItem it) {
        throw new UnsupportedOperationException("Guess what? It's not implemented!");
    }

    private Date asDate(Timestamp dueDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dueDate.getTime());
        return cal.getTime();
    }

    private Timestamp asTimestamp(Date dueDate) {
        return Timestamp.from(dueDate.toInstant());
    }
}
