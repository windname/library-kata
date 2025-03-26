package com.vg.exception;

public class BookConcurrencyException extends RuntimeException {
    public BookConcurrencyException(String message) {
        super(message);
    }
}