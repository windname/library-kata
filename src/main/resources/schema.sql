CREATE TABLE IF NOT EXISTS book (
    id UUID PRIMARY KEY,
    title VARCHAR(255),
    author VARCHAR(255),
    available BOOLEAN,
    version BIGINT
);

CREATE TABLE IF NOT EXISTS borrowed_book (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    book_id UUID NOT NULL,
    borrowed_at TIMESTAMP
);
