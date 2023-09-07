package com.yolt.providers.consorsbankgroup.common.ais.exception;

public class LoginNotFoundException extends RuntimeException {

    public LoginNotFoundException(final Throwable cause) {
        super(cause);
    }

    public LoginNotFoundException(final String message) {
        super(message);
    }
}
