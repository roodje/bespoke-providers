package com.yolt.providers.cbiglobe.common.exception;

public class PaymentFailedException extends RuntimeException {

    public PaymentFailedException(final String message) {
        super(message);
    }
}
