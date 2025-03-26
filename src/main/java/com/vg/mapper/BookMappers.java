package com.vg.mapper;

import com.vg.dto.BookRequestDTO;
import com.vg.dto.BookResponseDTO;
import com.vg.model.Book;

import java.util.UUID;
import java.util.function.Function;

public class BookMappers {
    public static final Function<Book, BookResponseDTO> bookToDto = book ->
        new BookResponseDTO(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.isAvailable()
        );

    public static final Function<BookRequestDTO, Book> toEntity = dto ->
            new Book(UUID.randomUUID(), dto.title(), dto.author(), true, null);

}
