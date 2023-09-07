package com.yolt.providers.openbanking.ais.exception;

import org.jose4j.jwt.consumer.InvalidJwtException;

public class InvalidSignatureException extends RuntimeException {

    public InvalidSignatureException(final String message) {
        super(message);
    }

    public InvalidSignatureException(final String message, InvalidJwtException e) {
        super(message, e);
    }

}
