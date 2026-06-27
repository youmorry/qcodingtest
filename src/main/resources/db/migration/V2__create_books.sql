CREATE TABLE books (
  id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  title              TEXT        NOT NULL,
  price              INTEGER     NOT NULL,
  publication_status TEXT        NOT NULL,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);
