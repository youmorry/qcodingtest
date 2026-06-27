CREATE TABLE author_books (
  author_id BIGINT NOT NULL REFERENCES authors (id),
  book_id   BIGINT NOT NULL REFERENCES books (id),
  PRIMARY KEY (author_id, book_id)
);
