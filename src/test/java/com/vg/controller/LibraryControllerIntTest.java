package com.vg.controller;

import com.vg.dto.BookRequestDTO;
import com.vg.dto.BookResponseDTO;
import com.vg.model.Book;
import com.vg.repository.BookRepository;
import com.vg.service.LibraryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LibraryControllerIntTest {
    private WebTestClient webTestClient;

    @Mock
    private LibraryService libraryService;

    @BeforeEach
    void setUp() {
        LibraryController controller = new LibraryController(libraryService);
        webTestClient = WebTestClient.bindToController(controller).build();
    }

    private final UUID testBookId = UUID.randomUUID();
    private final UUID testUserId = UUID.randomUUID();
    private final Book testBook = new Book(testBookId, "Test Book", "Author", true, 0L);

    @Test
    void getAllBooks() {
        when(libraryService.getAllBooks()).thenReturn(Flux.just(testBook));

        webTestClient.get().uri("/library/books")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(BookResponseDTO.class)
                .hasSize(1)
                .contains(new BookResponseDTO(testBookId, "Test Book", "Author", true));
    }

    @Test
    void addNewBook() {
        BookRequestDTO request = new BookRequestDTO("New Book", "New Author");
        when(libraryService.addBook(any())).thenReturn(Mono.just(testBook));

        webTestClient.post().uri("/library/book")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(BookResponseDTO.class)
                .isEqualTo(new BookResponseDTO(testBookId, "Test Book", "Author", true));
    }

    @Test
    void borrowBook() {
        Book borrowedBook = new Book(testBookId, "Test Book", "Author", false, 1L);
        when(libraryService.borrowBook(testBookId, testUserId)).thenReturn(Mono.just(borrowedBook));

        webTestClient.put().uri(uriBuilder -> uriBuilder.path("/library/borrow/{bookId}")
                        .queryParam("userId", testUserId).build(testBookId))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(BookResponseDTO.class).value(book -> {
                    assertEquals(testBookId, book.id());
                    assertEquals("Test Book", book.title());
                    assertEquals("Author", book.author());
                    assertFalse(book.available());
                });
    }

    @Test
    void returnBook() {
        Book returnedBook = new Book(testBookId, "Test Book", "Author", true, 1L);
        when(libraryService.returnBook(testBookId, testUserId)).thenReturn(Mono.just(returnedBook));

        webTestClient.put().uri(uriBuilder -> uriBuilder.path("/library/return/{bookId}")
                        .queryParam("userId", testUserId).build(testBookId))
                .exchange().expectStatus()
                .isOk()
                .expectBody(BookResponseDTO.class)
                .value(book -> {
                    assertEquals(testBookId, book.id());
                    assertEquals("Test Book", book.title());
                    assertEquals("Author", book.author());
                    assertTrue(book.available());
                });
    }

    @Test
    void getBorrowedBooksByUser() {
        when(libraryService.findBorrowedBooksByUser(testUserId)).thenReturn(Flux.empty()); // or Flux.just(...) with test data

        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/library/borrowed-books")
                        .queryParam("userId", testUserId).build())
                .exchange()
                .expectStatus()
                .isOk();
    }
}
