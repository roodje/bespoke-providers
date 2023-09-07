package com.yolt.providers.openbanking.ais.hsbcgroup.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.exception.ProviderRequestFailedException;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import org.springframework.web.client.HttpStatusCodeException;

import static org.springframework.http.HttpStatus.*;

public final class HsbcGroupUserSiteDeleteHandlerV2 implements HttpErrorHandler {

    public static final HsbcGroupUserSiteDeleteHandlerV2 GENERIC_HSBC_GROUP_USER_SITE_DELETE_HANDLER = new HsbcGroupUserSiteDeleteHandlerV2();

    @Override
    public void handle(final HttpStatusCodeException e) throws TokenInvalidException {
        if (isTokenInvalidException(e)) {
            throw new TokenInvalidException(String.format("Token invalid, received status %s.", e.getStatusCode().toString()));

        } else if (e.getStatusCode() != FORBIDDEN && e.getStatusCode() != BAD_REQUEST) {
            String msg = String.format("Unable to delete account-request for HSBC group. Received error code %s. Check RDD for body.",
                    e.getStatusCode());

            throw new ProviderRequestFailedException(msg);
        }
    }

    private boolean isTokenInvalidException(final HttpStatusCodeException e) {
        return UNAUTHORIZED.equals(e.getStatusCode()) || isInvalidGrant(e);
    }

    private boolean isInvalidGrant(final HttpStatusCodeException e) {
        return BAD_REQUEST.equals(e.getStatusCode()) && e.getResponseBodyAsString().contains("invalid_grant");
    }
}
