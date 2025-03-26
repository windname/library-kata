package com.vg.mapper;

import com.vg.dto.BorrowedBookViewDTO;
import com.vg.model.Book;
import com.vg.model.BorrowedBook;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BorrowedBookMappersTest {

    @Test
    void shouldMapBorrowedBookAndBookToBorrowedBookViewDTO() {
        UUID bookId = UUID.randomUUID();
        LocalDateTime borrowedAt = LocalDateTime.now();

        Book book = new Book(bookId, "Clean Code", "Robert C. Martin", false, 1L);
        BorrowedBook borrowed = new BorrowedBook(UUID.randomUUID(), UUID.randomUUID(), bookId, borrowedAt);

        BorrowedBookViewDTO dto = BorrowedBookMappers.borrowedBookToDto.apply(borrowed, book);

        assertThat(dto.bookId()).isEqualTo(bookId);
        assertThat(dto.title()).isEqualTo("Clean Code");
        assertThat(dto.author()).isEqualTo("Robert C. Martin");
        assertThat(dto.borrowedAt()).isEqualTo(borrowedAt);
    }
}
