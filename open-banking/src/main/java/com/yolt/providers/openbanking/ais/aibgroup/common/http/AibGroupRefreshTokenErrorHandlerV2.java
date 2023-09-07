package com.yolt.providers.openbanking.ais.aibgroup.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class AibGroupRefreshTokenErrorHandlerV2 implements HttpErrorHandler {

    public static final AibGroupRefreshTokenErrorHandlerV2 AIB_GROUP_REFRESH_TOKEN_ERROR_HANDLER = new AibGroupRefreshTokenErrorHandlerV2();
    private static final String EXPIRED_CONSENT_ERROR_CODE = "400-1200-007";
    private static final String INVALID_REFRESH_TOKEN_ERROR_CODE = "invalid_refresh_token";

    @Override
    public void handle(final HttpStatusCodeException e) throws TokenInvalidException {
        if (isTokenInvalidException(e) || isForbidden(e) || isConsentExpired(e) || isRefreshTokenInvalid(e)) {
            throw new TokenInvalidException(String.format("Token invalid, received status %s.", e.getStatusCode().toString()));
        } else {
            throw e;
        }
    }

    private boolean isTokenInvalidException(final HttpStatusCodeException e) {
        return e.getStatusCode() == HttpStatus.UNAUTHORIZED || isInvalidGrant(e);
    }

    private boolean isInvalidGrant(final HttpStatusCodeException e) {
        return e.getStatusCode() == HttpStatus.BAD_REQUEST &&
                e.getResponseBodyAsString().contains("invalid_grant");
    }

    private boolean isForbidden(final HttpStatusCodeException e) {
        return e.getStatusCode() == HttpStatus.FORBIDDEN;
    }

    private boolean isConsentExpired(final HttpStatusCodeException e) {
        return e.getStatusCode() == HttpStatus.BAD_REQUEST &&
                e.getResponseBodyAsString().contains(EXPIRED_CONSENT_ERROR_CODE);
    }

    private boolean isRefreshTokenInvalid(HttpStatusCodeException e) {
        return e.getStatusCode() == HttpStatus.BAD_REQUEST &&
                e.getResponseBodyAsString().contains(INVALID_REFRESH_TOKEN_ERROR_CODE);
    }
}
