package com.yolt.providers.openbanking.ais.tidegroup.common;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public final class TideHttpErrorHandler implements HttpErrorHandler {

    public static final TideHttpErrorHandler TIDE_HTTP_ERROR_HANDLER = new TideHttpErrorHandler();

    @Override
    public void handle(final HttpStatusCodeException e) throws TokenInvalidException {
        if (isTokenInvalidException(e)) {
            throw new TokenInvalidException(String.format("Token invalid, received status %s.", e.getStatusCode().toString()));
        } else {
            throw e;
        }
    }

    private boolean isTokenInvalidException(final HttpStatusCodeException e) {
        return e.getStatusCode() == HttpStatus.UNAUTHORIZED || isInvalidGrant(e) || isForbidden(e) || isRevoked(e);
    }

    private boolean isInvalidGrant(final HttpStatusCodeException e) {
        return e.getStatusCode() == HttpStatus.BAD_REQUEST && e.getResponseBodyAsString().contains("invalid_grant");
    }

    private boolean isForbidden(final HttpStatusCodeException e) {
        return e.getStatusCode() == HttpStatus.FORBIDDEN;
    }

    private boolean isRevoked(HttpStatusCodeException e) {
        return e.getStatusCode() == HttpStatus.BAD_REQUEST && e.getResponseBodyAsString().contains("has status Revoked. Expected 'Authorised'");
    }
}
