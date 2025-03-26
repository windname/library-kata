package com.vg.controller;

import com.vg.dto.BookRequestDTO;
import com.vg.dto.BookResponseDTO;
import com.vg.dto.BorrowedBookViewDTO;
import com.vg.model.Book;
import com.vg.repository.BookRepository;
import com.vg.service.LibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static com.vg.mapper.BookMappers.toEntity;
import static com.vg.mapper.BookMappers.bookToDto;
import static com.vg.mapper.BorrowedBookMappers.borrowedBookToDto;

import java.util.UUID;

@RestController
@RequestMapping("/library")
@Tag(name = "Library API", description = "Operations for borrowing and returning books")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @Operation(summary = "List of books")
    @GetMapping("/books")
    public Flux<BookResponseDTO> getAllBooks() {
        return libraryService.getAllBooks().map(bookToDto);
    }

    @Operation(summary = "Add a new book to the library")
    @PostMapping("/book")
    public Mono<BookResponseDTO> addBook(@RequestBody BookRequestDTO dto) {
        return libraryService.addBook(toEntity.apply(dto))
                .map(bookToDto);
    }

    @Operation(summary = "Borrow a book by ID for a user")
    @PutMapping("/borrow/{bookId}")
    public Mono<BookResponseDTO> borrowBook(@PathVariable UUID bookId, @RequestParam UUID userId) {
        return libraryService.borrowBook(bookId, userId).map(bookToDto);
    }

    @Operation(summary = "Return a borrowed book by ID for a user")
    @PutMapping("/return/{bookId}")
    public Mono<BookResponseDTO> returnBook(@PathVariable UUID bookId, @RequestParam UUID userId) {
        return libraryService.returnBook(bookId, userId).map(bookToDto);
    }

    @Operation(summary = "Get list of books currently borrowed by a user")
    @GetMapping("/borrowed-books")
    public Flux<BorrowedBookViewDTO> getBorrowedBooksByUser(@RequestParam UUID userId) {
        return libraryService.findBorrowedBooksByUser(userId)
                .map(tuple -> borrowedBookToDto.apply(tuple.getT1(), tuple.getT2()));
    }
}
