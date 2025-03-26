package com.vg.repository;

import com.vg.model.BorrowedBook;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import java.util.UUID;

public interface BorrowedBookRepository extends ReactiveCrudRepository<BorrowedBook, UUID> {
    Flux<BorrowedBook> findByUserId(UUID userId);

    Flux<BorrowedBook> findByUserIdAndBookId(UUID userId, UUID bookId);
}
