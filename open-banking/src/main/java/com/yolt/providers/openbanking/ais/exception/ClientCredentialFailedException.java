package com.yolt.providers.openbanking.ais.exception;

public class ClientCredentialFailedException extends RuntimeException {

    public ClientCredentialFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
