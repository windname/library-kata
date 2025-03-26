package com.vg.service;

import com.vg.exception.BookConcurrencyException;
import com.vg.exception.BookUnavailableException;
import com.vg.model.Book;
import com.vg.repository.BookRepository;
import com.vg.repository.BorrowedBookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

@DataR2dbcTest
@Import(LibraryService.class)
public class LibraryServiceIntTest {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BorrowedBookRepository borrowedBookRepository;

    @Autowired
    private LibraryService libraryService;

    private final UUID user1 = UUID.randomUUID();
    private final UUID user2 = UUID.randomUUID();
    private final UUID bookId = UUID.randomUUID();

    private Book book;

    @BeforeEach
    public void setup() {
        book = new Book(bookId, "Concurrent Book", "Author", true, null);
        bookRepository.deleteAll()
                .then(borrowedBookRepository.deleteAll())
                .then(bookRepository.save(book))
                .block();
    }

    @Test
    void throwErrorborrowDueToConcurrency() {
        Mono<Book> firstBorrow = libraryService.borrowBook(bookId, user1).cache(); // caching first result
        Mono<Book> secondBorrow = libraryService.borrowBook(bookId, user2);

        StepVerifier.create(firstBorrow)
                .expectNextMatches(b -> !b.isAvailable() && b.getId().equals(bookId))
                .verifyComplete();

        StepVerifier.create(secondBorrow)
                .expectErrorMatches(error ->
                        (error instanceof BookUnavailableException &&
                                "Book is already borrowed.".equals(error.getMessage())) ||
                                (error instanceof BookConcurrencyException &&
                                        "Book was just borrowed by another user.".equals(error.getMessage()))
                )
                .verify();
    }

    @Test
    void optimisticLockingDirectlyTest() {
        Book book = new Book(bookId, "Title", "Author", true, 0L);
        bookRepository.save(book).block();

        // Simulate concurrent reads
        Book first = bookRepository.findById(bookId).block();
        Book second = bookRepository.findById(bookId).block();

        // First update
        first.setAvailable(false);
        bookRepository.save(first).block();

        // Second update → version conflict
        second.setAvailable(false);
        StepVerifier.create(bookRepository.save(second))
                .expectError(OptimisticLockingFailureException.class)
                .verify();
    }
}
