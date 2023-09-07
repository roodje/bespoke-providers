package com.yolt.providers.direkt1822group.common.exception;

public class LoginNotFoundException extends RuntimeException {

    public LoginNotFoundException(final Throwable e) {
        super(e);
    }

    public LoginNotFoundException(String message) {
        super(message);
    }
}
