package com.yolt.providers.sparkassenandlandesbanks.common.exception;

public class LoginNotFoundException extends RuntimeException {
    public LoginNotFoundException(final Throwable e) {
        super(e);
    }
}
