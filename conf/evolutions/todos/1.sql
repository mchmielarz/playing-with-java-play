# --- Creation OF todos TABLE

# --- !Ups
create table todos (
  "item_id" VARCHAR(36) CONSTRAINT id_key PRIMARY KEY,
  "item_name" TEXT NOT NULL,
  "status" TEXT NOT NULL,
  "due_date" CHAR(18) NOT NULL
);

# --- !Downs
drop table todos;