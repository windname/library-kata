package com.vg.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vg.exception.BookConcurrencyException;
import com.vg.exception.BookNotFoundException;
import com.vg.exception.BookUnavailableException;
import com.vg.exception.BorrowRecordNotFoundException;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
@Order(-2)
public class GlobalWebFluxExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalWebFluxExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        return Mono.just(exchange)
                .doOnNext(e -> prepareResponse(e, ex))
                .flatMap(e -> serializeError(ex))
                .flatMap(bytes -> writeResponse(exchange, bytes))
                .onErrorResume(JsonProcessingException.class, this::handleSerializationError);
    }

    private void prepareResponse(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = determineStatus(ex);
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
    }

    private Mono<byte[]> serializeError(Throwable ex) {
        return Mono.fromCallable(() ->
                objectMapper.writeValueAsBytes(
                        new ErrorResponse(ex.getMessage(), determineStatus(ex))
                ));
    }

    private Mono<Void> writeResponse(ServerWebExchange exchange, byte[] bytes) {
        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(bytes)));
    }

    private Mono<Void> handleSerializationError(JsonProcessingException ex) {
        return Mono.error(ex);
    }

    private HttpStatus determineStatus(Throwable ex) {
        if (ex instanceof BookNotFoundException) return HttpStatus.NOT_FOUND;
        if (ex instanceof BookConcurrencyException) return HttpStatus.CONFLICT;
        if (ex instanceof BookUnavailableException) return HttpStatus.CONFLICT;
        if (ex instanceof BorrowRecordNotFoundException) return HttpStatus.CONFLICT;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public record ErrorResponse(
            String message,
            String error,
            int status,
            Instant timestamp
    ) {
        public ErrorResponse(String message, HttpStatus status) {
            this(
                    message,
                    status.getReasonPhrase(),
                    status.value(),
                    Instant.now()
            );
        }
    }
}