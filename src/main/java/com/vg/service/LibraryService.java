package com.vg.service;

import com.vg.exception.BookConcurrencyException;
import com.vg.exception.BookNotFoundException;
import com.vg.exception.BookUnavailableException;
import com.vg.model.Book;
import com.vg.model.BorrowedBook;
import com.vg.repository.BookRepository;
import com.vg.repository.BorrowedBookRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LibraryService {

    private static final Logger log = LoggerFactory.getLogger(LibraryService.class);

    private final BookRepository bookRepository;
    private final BorrowedBookRepository borrowedBookRepository;

    public LibraryService(BookRepository bookRepository, BorrowedBookRepository borrowedBookRepository) {
        this.bookRepository = bookRepository;
        this.borrowedBookRepository = borrowedBookRepository;
    }

    public Flux<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Mono<Book> addBook(Book book) {
        log.info("Adding new book: {} by {}", book.getTitle(), book.getAuthor());
        book.setId(UUID.randomUUID());
        book.setAvailable(true);
        return bookRepository.save(book);
    }

    public Mono<Book> borrowBook(UUID bookId, UUID userId) {
        log.info("User {} requested to borrow book {}", userId, bookId);
        return bookRepository.findById(bookId)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Book not found: {}", bookId);
                    return Mono.error(new BookNotFoundException("Book not found"));
                }))
                .flatMap(book -> {
                    if (!book.isAvailable()) {
                        log.warn("Book {} is already borrowed", bookId);
                        return Mono.error(new BookUnavailableException("Book is already borrowed."));
                    }

                    book.setAvailable(false);

                    return bookRepository.save(book)
                            .flatMap(savedBook ->
                                    borrowedBookRepository.save(
                                            new BorrowedBook(UUID.randomUUID(), userId, savedBook.getId(), LocalDateTime.now())
                                    ).thenReturn(savedBook)
                            );
                })
                .onErrorMap(OptimisticLockingFailureException.class,
                        ex -> new BookConcurrencyException("Book was just borrowed by another user."));
    }

    public Mono<Book> returnBook(UUID bookId, UUID userId) {
        log.info("User {} is returning book {}", userId, bookId);
        return bookRepository.findById(bookId)
                .switchIfEmpty(Mono.error(new BookNotFoundException("Book not found: " + bookId)))
                .flatMap(book -> {
                    if (book.isAvailable()) {
                        log.warn("User {} cannot return book {}. It's available", userId, bookId);
                        return Mono.error(new BookUnavailableException("Book is already marked as available."));
                    }

                    book.setAvailable(true);

                    return borrowedBookRepository.findByUserIdAndBookId(userId, bookId)
                            .switchIfEmpty(Mono.error(new BookUnavailableException("No borrow record found.")))
                            .next()
                            .flatMap(borrowedBook ->
                                    borrowedBookRepository.delete(borrowedBook)
                                            .then(bookRepository.save(book))
                            )
                            .thenReturn(book);
                });
    }

    public Flux<Tuple2<BorrowedBook, Book>> findBorrowedBooksByUser(UUID userId) {
        return borrowedBookRepository.findByUserId(userId)
                .flatMap(borrowedBook ->
                        bookRepository.findById(borrowedBook.getBookId())
                                .map(book -> Tuples.of(borrowedBook, book))
                                .switchIfEmpty(Mono.fromRunnable(() -> {
                                            log.warn("Book not found for borrowed record {}", borrowedBook.getId());
                                        })
                                        .then(Mono.empty()))
                );
    }
}
