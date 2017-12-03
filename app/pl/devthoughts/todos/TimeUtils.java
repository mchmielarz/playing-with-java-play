package pl.devthoughts.todos;

import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static pl.devthoughts.todos.TodosConfig.DUE_DATE_FORMAT;

public class TimeUtils {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DUE_DATE_FORMAT);

  public static Timestamp asTimestamp(String dueDate) {
    return Timestamp.newBuilder().setSeconds(asSeconds(dueDate)).build();
  }

  public static LocalDateTime fromString(String date) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DUE_DATE_FORMAT);
    return LocalDateTime.parse(date, formatter);
  }

  public static Timestamp asProtobufTimestamp(LocalDateTime dueDate) {
    Instant instant = dueDate.atZone(ZoneId.systemDefault()).toInstant();
    return Timestamp.newBuilder()
                    .setSeconds(instant.getEpochSecond())
                    .setNanos(instant.getNano())
                    .build();
  }

  public static LocalDateTime asLocalDateTime(Timestamp timestamp) {
    return LocalDateTime
        .ofInstant(Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()),
                   ZoneId.systemDefault());
  }

  public static LocalDateTime asLocalDateTime(java.sql.Timestamp dueDate) {
    return dueDate.toLocalDateTime();
  }

  public static java.sql.Timestamp asTimestamp(LocalDateTime dueDate) {
    return java.sql.Timestamp.valueOf(dueDate);
  }

  private static long asSeconds(String date) {
    final LocalDateTime ldt = LocalDateTime.parse(date, FORMATTER);
    return ldt.atZone(ZoneId.systemDefault()).toEpochSecond();
  }

}
