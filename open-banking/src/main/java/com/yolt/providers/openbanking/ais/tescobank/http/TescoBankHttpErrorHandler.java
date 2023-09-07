package com.yolt.providers.openbanking.ais.tescobank.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public final class TescoBankHttpErrorHandler implements HttpErrorHandler {

    public static final TescoBankHttpErrorHandler TESCO_BANK_HTTP_ERROR_HANDLER = new TescoBankHttpErrorHandler();

    @Override
    public void handle(final HttpStatusCodeException e) throws TokenInvalidException {
        if (isUnauthorized(e) || isForbidden(e) || isInvalidGrant(e) || isMissingConsent(e)) {
            throw new TokenInvalidException(String.format("Token invalid, received status %s.", e.getStatusCode()));
        } else {
            throw e;
        }
    }

    private boolean isUnauthorized(final HttpStatusCodeException e) {
        return e.getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    private boolean isInvalidGrant(final HttpStatusCodeException e) {
        return e.getStatusCode() == HttpStatus.BAD_REQUEST && e.getResponseBodyAsString().contains("invalid_grant");
    }

    private boolean isMissingConsent(final HttpStatusCodeException e) {
        return e.getStatusCode() == HttpStatus.BAD_REQUEST
                && e.getResponseBodyAsString().contains("\"Message\":\"Consent not found\"");
    }

    private boolean isForbidden(final HttpStatusCodeException e) {
        return e.getStatusCode() == HttpStatus.FORBIDDEN;
    }
}
