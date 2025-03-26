package com.vg.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vg.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalWebFluxExceptionHandlerTest {

    @Mock private ObjectMapper objectMapper;
    @Mock private ServerWebExchange exchange;
    @InjectMocks private GlobalWebFluxExceptionHandler exceptionHandler;

    private MockServerHttpResponse response;

    @BeforeEach
    void setUp() {
        response = new MockServerHttpResponse(new DefaultDataBufferFactory());
        when(exchange.getResponse()).thenReturn(response);
    }

    @Test
    void handleBookNotFoundException_shouldReturnNotFoundStatus() throws Exception {
        testExceptionHandling(
                new BookNotFoundException("BookNotFoundException"),
                HttpStatus.NOT_FOUND
        );
    }

    @Test
    void handleBookConcurrencyException_shouldReturnConflictStatus() throws Exception {
        testExceptionHandling(
                new BookConcurrencyException("BookConcurrencyException"),
                HttpStatus.CONFLICT
        );
    }

    @Test
    void handleBorrowRecordException() throws Exception {
        testExceptionHandling(
                new BorrowRecordNotFoundException("BorrowRecordNotFoundException"),
                HttpStatus.CONFLICT
        );
    }

    @Test
    void handleGenericException_shouldReturnInternalServerError() throws Exception {
        testExceptionHandling(
                new Exception("Unexpected error"),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @Test
    void handle_whenJsonSerializationFails_shouldPropagateError() throws Exception {
        BookNotFoundException ex = new BookNotFoundException("Book not found");
        when(objectMapper.writeValueAsBytes(any()))
                .thenThrow(new JsonProcessingException("Serialization failed") {});

        StepVerifier.create(exceptionHandler.handle(exchange, ex))
                .expectError(JsonProcessingException.class)
                .verify();
    }

    private void testExceptionHandling(Throwable ex, HttpStatus expectedStatus) throws Exception {
        String expectedJson = String.format(
                "{\"message\":\"%s\",\"error\":\"%s\",\"status\":%d}",
                ex.getMessage(),
                expectedStatus.getReasonPhrase(),
                expectedStatus.value()
        );

        when(objectMapper.writeValueAsBytes(any()))
                .thenReturn(expectedJson.getBytes(StandardCharsets.UTF_8));

        StepVerifier.create(exceptionHandler.handle(exchange, ex))
                .verifyComplete();

        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(getResponseBodyAsString()).isEqualTo(expectedJson);
    }

    private String getResponseBodyAsString() {
        return response.getBodyAsString()
                .defaultIfEmpty("")
                .block();
    }
}