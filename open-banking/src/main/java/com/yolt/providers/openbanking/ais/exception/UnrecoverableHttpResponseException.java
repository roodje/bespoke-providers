package com.yolt.providers.openbanking.ais.exception;

/**
 * An exception that is thrown when a non 2xx response code is received from a provider, and NO retry is able to solve this issue.
 * In most situations this requires a manual debug session or inspection of logs to see how the issue can be solved.
 */
public class UnrecoverableHttpResponseException extends RuntimeException {

    public UnrecoverableHttpResponseException(String message) {
        super(message);
    }
}
