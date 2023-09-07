package com.yolt.providers.ing.common.exception;

public class ClientCredentialsFailedException extends RuntimeException {

    public ClientCredentialsFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}