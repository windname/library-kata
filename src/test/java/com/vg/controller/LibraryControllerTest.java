package com.vg.controller;

import com.vg.dto.BookRequestDTO;
import com.vg.dto.BookResponseDTO;
import com.vg.model.Book;
import com.vg.repository.BookRepository;
import com.vg.service.LibraryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class LibraryControllerTest {

    private final LibraryService libraryService = Mockito.mock(LibraryService.class);
    private final LibraryController controller = new LibraryController(libraryService);

    @Test
    void returnAllBooks() {
        Book book = new Book(UUID.randomUUID(), "Test Book", "Author", true, 0L);
        when(libraryService.getAllBooks()).thenReturn(Flux.just(book));

        Flux<BookResponseDTO> result = controller.getAllBooks();

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.title().equals("Test Book"))
                .verifyComplete();
    }

    @Test
    void createNewBook() {
        BookRequestDTO request = new BookRequestDTO("New Book", "New Author");
        Book savedBook = new Book(UUID.randomUUID(), "New Book", "New Author", true, 0L);
        when(libraryService.addBook(any())).thenReturn(Mono.just(savedBook));

        Mono<BookResponseDTO> result = controller.addBook(request);

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.title().equals("New Book"))
                .verifyComplete();
    }

    @Test
    void returnUpdatedBook() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Book borrowedBook = new Book(bookId, "Book", "Author", false, 1L);
        when(libraryService.borrowBook(bookId, userId)).thenReturn(Mono.just(borrowedBook));

        Mono<BookResponseDTO> result = controller.borrowBook(bookId, userId);

        StepVerifier.create(result)
                .expectNextMatches(book -> !book.available())
                .verifyComplete();
    }

    @Test
    void handleErrorOnBorrowBook() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(libraryService.borrowBook(bookId, userId))
                .thenReturn(Mono.error(new RuntimeException("Concurrency error")));

        Mono<BookResponseDTO> result = controller.borrowBook(bookId, userId);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void returndBookSuccessfully() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Book returnedBook = new Book(bookId, "Book", "Author", true, 1L);
        when(libraryService.returnBook(bookId, userId)).thenReturn(Mono.just(returnedBook));

        Mono<BookResponseDTO> result = controller.returnBook(bookId, userId);

        StepVerifier.create(result)
                .expectNextMatches(BookResponseDTO::available)
                .verifyComplete();
    }
}