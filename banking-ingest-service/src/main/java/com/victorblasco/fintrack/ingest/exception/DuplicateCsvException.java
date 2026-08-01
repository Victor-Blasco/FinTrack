package com.victorblasco.fintrack.ingest.exception;

public class DuplicateCsvException extends RuntimeException {
    public DuplicateCsvException(String message) {
        super(message);
    }
}
