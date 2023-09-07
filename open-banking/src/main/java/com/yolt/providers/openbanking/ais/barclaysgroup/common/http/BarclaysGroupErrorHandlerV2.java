package com.yolt.providers.openbanking.ais.barclaysgroup.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class BarclaysGroupErrorHandlerV2 implements HttpErrorHandler {

    public static final BarclaysGroupErrorHandlerV2 BARCLAYS_GROUP_ERROR_HANDLER = new BarclaysGroupErrorHandlerV2();
    private static final String INVALID_CONSENT_MESSAGE_PART = "UK.OBIE.Resource.InvalidConsentStatus";

    @Override
    public void handle(final HttpStatusCodeException e) throws TokenInvalidException {
        if (isTokenInvalidException(e) || isForbidden(e) || isConsentInvalid(e)) {
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

    private boolean isConsentInvalid(HttpStatusCodeException e) {
        return HttpStatus.BAD_REQUEST.equals(e.getStatusCode()) && e.getResponseBodyAsString().contains(INVALID_CONSENT_MESSAGE_PART);
    }
}
