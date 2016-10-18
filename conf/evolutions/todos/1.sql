# --- Creation OF todos TABLE

# --- !Ups
create table todos (
  "id" TEXT NOT NULL UNIQUE PRIMARY KEY,
  "name" TEXT NOT NULL,
  "status" TEXT NOT NULL,
  "dueDate" TIMESTAMP NOT NULL
);

# --- !Downs
drop table todos;