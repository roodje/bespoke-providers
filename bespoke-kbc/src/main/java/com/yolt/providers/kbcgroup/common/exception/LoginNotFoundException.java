package com.yolt.providers.kbcgroup.common.exception;

public class LoginNotFoundException extends RuntimeException {
    public LoginNotFoundException(final Throwable e) {
        super(e);
    }
}
