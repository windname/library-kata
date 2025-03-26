package com.vg.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("borrowed_book")
public class BorrowedBook implements Persistable<UUID> {
    @Id
    private UUID id;
    private UUID userId;
    private UUID bookId;
    private LocalDateTime borrowedAt;
    @Transient
    private boolean isNew = true;

    public BorrowedBook() {}

    public BorrowedBook(UUID id, UUID userId, UUID bookId, LocalDateTime borrowedAt) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.borrowedAt = borrowedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }

    public LocalDateTime getBorrowedAt() {
        return borrowedAt;
    }

    public void setBorrowedAt(LocalDateTime borrowedAt) {
        this.borrowedAt = borrowedAt;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void markNotNew() {
        this.isNew = false;
    }
}
