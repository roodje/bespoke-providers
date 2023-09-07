package com.yolt.providers.openbanking.ais.exception;

/**
 * An exception that is thrown when a non 2xx response code is received from a provider.
 * This exception should ONLY be thrown in situations where a simple refresh might solve the issue.
 */
public class ProviderRequestFailedException extends RuntimeException {

    public ProviderRequestFailedException(String message) {
        this(message, null);
    }

    public ProviderRequestFailedException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
