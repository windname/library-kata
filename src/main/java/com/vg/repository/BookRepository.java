package com.vg.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import com.vg.model.Book;
import reactor.core.publisher.Flux;
import java.util.UUID;

public interface BookRepository extends R2dbcRepository<Book, UUID> {
    // Add custom paged query
    Flux<Book> findAllBy(Pageable pageable);
}
