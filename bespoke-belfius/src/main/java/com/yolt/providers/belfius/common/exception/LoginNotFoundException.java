package com.yolt.providers.belfius.common.exception;

public class LoginNotFoundException extends RuntimeException {

    public LoginNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
