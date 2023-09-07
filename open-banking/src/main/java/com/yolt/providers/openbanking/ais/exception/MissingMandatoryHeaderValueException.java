package com.yolt.providers.openbanking.ais.exception;

/**
 * Indicates that a mandatory Open Banking request header is missing in the built request.
 */
public class MissingMandatoryHeaderValueException extends RuntimeException {

    public MissingMandatoryHeaderValueException(String message) {
        super(message);
    }
}
