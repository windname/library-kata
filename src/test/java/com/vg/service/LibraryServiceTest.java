package com.vg.service;

import com.vg.exception.BookConcurrencyException;
import com.vg.exception.BookNotFoundException;
import com.vg.exception.BookUnavailableException;
import com.vg.model.Book;
import com.vg.model.BorrowedBook;
import com.vg.repository.BookRepository;
import com.vg.repository.BorrowedBookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LibraryServiceTest {

    private BookRepository bookRepository;
    private BorrowedBookRepository borrowedBookRepository;
    private LibraryService libraryService;

    @BeforeEach
    void setUp() {
        bookRepository = mock(BookRepository.class);
        borrowedBookRepository = mock(BorrowedBookRepository.class);
        libraryService = new LibraryService(bookRepository, borrowedBookRepository);
    }

    @Test
    void addBookSuccessfully() {
        Book book = new Book(null, "Title", "Author", true, null);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book b = invocation.getArgument(0);
            b.setId(UUID.randomUUID());
            return Mono.just(b);
        });

        StepVerifier.create(libraryService.addBook(book)).assertNext(b -> {
            assert b.getId() != null;
            assert b.isAvailable();
        }).verifyComplete();
    }

    @Test
    void returnBookSuccessfully() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Book book = new Book(bookId, "Title", "Author", false, null);
        BorrowedBook borrowedBook = new BorrowedBook(UUID.randomUUID(), userId, bookId, LocalDateTime.now());

        when(bookRepository.findById(bookId)).thenReturn(Mono.just(book));
        when(bookRepository.save(any(Book.class))).thenReturn(Mono.just(book));
        when(borrowedBookRepository.findByUserIdAndBookId(userId, bookId)).thenReturn(Flux.just(borrowedBook));
        when(borrowedBookRepository.delete(any())).thenReturn(Mono.empty());

        StepVerifier.create(libraryService.returnBook(bookId, userId))
                .expectNext(book).verifyComplete();
    }

    @Test
    void throwWhenReturningAvailableBook() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Book book = new Book(bookId, "Title", "Author", true, null);

        when(bookRepository.findById(bookId)).thenReturn(Mono.just(book));

        StepVerifier.create(libraryService.returnBook(bookId, userId))
                .expectError(BookUnavailableException.class).verify();
    }

    @Test
    void findBorrowedBooksByUser() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        BorrowedBook borrowedBook = new BorrowedBook(UUID.randomUUID(), userId, bookId, LocalDateTime.now());
        Book book = new Book(bookId, "Title", "Author", false, null);

        when(borrowedBookRepository.findByUserId(userId)).thenReturn(Flux.just(borrowedBook));
        when(bookRepository.findById(bookId)).thenReturn(Mono.just(book));

        StepVerifier.create(libraryService.findBorrowedBooksByUser(userId))
                .expectNextMatches(tuple ->
                        tuple.getT1().equals(borrowedBook) && tuple.getT2().equals(book))
                .verifyComplete();
    }

    @Test
    void borrowBookSuccessfully() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Book book = new Book(bookId, "Title", "Author", true, null);

        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        Book savedBook = new Book(bookId, "Title", "Author", false, null); // expected state after borrow

        when(bookRepository.findById(bookId)).thenReturn(Mono.just(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book toSave = invocation.getArgument(0);
            return Mono.just(toSave); // simulate saving modified book
        });
        when(borrowedBookRepository.save(any(BorrowedBook.class)))
                .thenReturn(Mono.just(mock(BorrowedBook.class)));

        StepVerifier.create(libraryService.borrowBook(bookId, userId)).assertNext(result -> {
            assertFalse(result.isAvailable(), "Book should be marked as not available");
            assertEquals(bookId, result.getId());
        }).verifyComplete();

        verify(bookRepository).save(bookCaptor.capture());
        assertFalse(bookCaptor.getValue().isAvailable(), "Saved book should be unavailable");
    }

    @Test
    void throwWhenBorrowBookNotFound() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(bookRepository.findById(bookId)).thenReturn(Mono.empty());

        StepVerifier.create(libraryService.borrowBook(bookId, userId))
                .expectError(BookNotFoundException.class).verify();
    }

    @Test
    void throwWhenBookAlreadyBorrowed() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Book book = new Book(bookId, "Title", "Author", false, null);

        when(bookRepository.findById(bookId)).thenReturn(Mono.just(book));

        StepVerifier.create(libraryService.borrowBook(bookId, userId))
                .expectError(BookUnavailableException.class).verify();
    }

    @Test
    void shouldThrowConcurrencyExceptionWhenBookBorrowedConcurrently() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Book book = new Book(bookId, "Title", "Author", true, null);
        when(bookRepository.findById(bookId)).thenReturn(Mono.just(book));
        when(bookRepository.save(any(Book.class)))
                .thenReturn(Mono.error(new OptimisticLockingFailureException("Concurrent update")));
        when(borrowedBookRepository.save(any())).thenReturn(Mono.empty());

        StepVerifier.create(libraryService.borrowBook(bookId, userId))
                .expectError(BookConcurrencyException.class).verify();
    }

}