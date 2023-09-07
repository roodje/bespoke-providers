package com.yolt.providers.openbanking.ais.exception;

public class LoginNotFoundException extends RuntimeException {

    public LoginNotFoundException(final Throwable e) {
        super(e);
    }

}
