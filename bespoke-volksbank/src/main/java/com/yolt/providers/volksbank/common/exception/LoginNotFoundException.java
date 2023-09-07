package com.yolt.providers.volksbank.common.exception;

public class LoginNotFoundException extends RuntimeException {

    public LoginNotFoundException(final Throwable e) {
        super(e);
    }
}