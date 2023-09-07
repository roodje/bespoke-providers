package com.yolt.providers.openbanking.ais.generic2.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public final class DefaultHttpErrorHandler implements HttpErrorHandler {

    public static final DefaultHttpErrorHandler DEFAULT_HTTP_ERROR_HANDLER = new DefaultHttpErrorHandler();

    @Override
    public void handle(final HttpStatusCodeException e) throws TokenInvalidException {
        if (isTokenInvalidException(e) || isForbidden(e)) {
            throw new TokenInvalidException(String.format("Token invalid, received status %s.", e.getStatusCode().toString()));
        } else {
            throw e;
        }
    }

    private boolean isTokenInvalidException(final HttpStatusCodeException e) {
        return e.getStatusCode() == HttpStatus.UNAUTHORIZED ||
                isInvalidGrant(e);
    }

    private boolean isInvalidGrant(final HttpStatusCodeException e) {
        return e.getStatusCode() == HttpStatus.BAD_REQUEST && e.getResponseBodyAsString().contains("invalid_grant");
    }

    private boolean isForbidden(final HttpStatusCodeException e) {
        return e.getStatusCode() == HttpStatus.FORBIDDEN;
    }
}
