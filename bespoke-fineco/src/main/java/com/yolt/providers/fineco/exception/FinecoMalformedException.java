package com.yolt.providers.fineco.exception;

public class FinecoMalformedException extends RuntimeException {
    public FinecoMalformedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
