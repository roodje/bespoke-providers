package com.yolt.providers.redsys.common.exception;

public class RedsysLoginNotFoundException extends RuntimeException {
    public RedsysLoginNotFoundException(final String message) {
        super(message);
    }

    public RedsysLoginNotFoundException(final Throwable e) {
        super(e);
    }
}
