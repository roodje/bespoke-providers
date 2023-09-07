package com.yolt.providers.openbanking.ais.generic2.pec.common.exception;

public class GenericPaymentRequestInvocationException extends RuntimeException {

    public GenericPaymentRequestInvocationException(Throwable cause) {
        super(cause);
    }

    public GenericPaymentRequestInvocationException(String message) {
        super(message);
    }
}
