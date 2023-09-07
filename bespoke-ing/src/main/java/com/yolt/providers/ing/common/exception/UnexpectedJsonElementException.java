package com.yolt.providers.ing.common.exception;

public class UnexpectedJsonElementException extends RuntimeException {

    public UnexpectedJsonElementException(final String message) {
        super(message);
    }
}