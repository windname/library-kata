package com.vg.mapper;

import com.vg.dto.BorrowedBookViewDTO;
import com.vg.model.Book;
import com.vg.model.BorrowedBook;

import java.util.function.BiFunction;

public class BorrowedBookMappers {
    public static final BiFunction<BorrowedBook, Book, BorrowedBookViewDTO> borrowedBookToDto = (borrowed, book) ->
        new BorrowedBookViewDTO(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            borrowed.getBorrowedAt()
        );
}
