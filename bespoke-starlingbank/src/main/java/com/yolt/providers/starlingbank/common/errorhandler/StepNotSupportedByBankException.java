package com.yolt.providers.starlingbank.common.errorhandler;

public class StepNotSupportedByBankException extends RuntimeException {
    public StepNotSupportedByBankException(final String message) {
        super(message);
    }
}
