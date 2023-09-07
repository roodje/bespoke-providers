package com.yolt.providers.openbanking.ais.exception;

public class UnexpectedJsonElementException extends RuntimeException {

    public UnexpectedJsonElementException(final String message) {
        super(message);
    }

    public UnexpectedJsonElementException(final Throwable throwable) {
        super(throwable);
    }

    public UnexpectedJsonElementException(final String message, Exception e) {
        super(message, e);
    }

}
