package com.vg.mapper;

import com.vg.dto.BookRequestDTO;
import com.vg.dto.BookResponseDTO;
import com.vg.model.Book;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BookMappersTest {

    @Test
    void shouldMapBookToBookResponseDto() {
        UUID bookId = UUID.randomUUID();
        Book book = new Book(bookId, "Title", "Author", true, 1L);

        BookResponseDTO dto = BookMappers.bookToDto.apply(book);

        assertThat(dto.id()).isEqualTo(bookId);
        assertThat(dto.title()).isEqualTo("Title");
        assertThat(dto.author()).isEqualTo("Author");
        assertThat(dto.available()).isTrue();
    }

    @Test
    void shouldMapBookRequestDtoToBookEntity() {
        BookRequestDTO requestDTO = new BookRequestDTO("Effective Java", "Joshua Bloch");

        Book book = BookMappers.toEntity.apply(requestDTO);

        assertThat(book.getId()).isNotNull();
        assertThat(book.getTitle()).isEqualTo("Effective Java");
        assertThat(book.getAuthor()).isEqualTo("Joshua Bloch");
        assertThat(book.isAvailable()).isTrue();
        assertThat(book.getVersion()).isNull();
    }
}
