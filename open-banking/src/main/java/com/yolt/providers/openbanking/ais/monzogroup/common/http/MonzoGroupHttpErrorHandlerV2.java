package com.yolt.providers.openbanking.ais.monzogroup.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Arrays;
import java.util.List;

public final class MonzoGroupHttpErrorHandlerV2 implements HttpErrorHandler {

    public static final MonzoGroupHttpErrorHandlerV2 MONZO_GROUP_ERROR_HANDLER = new MonzoGroupHttpErrorHandlerV2();

    private static final List<String> INVALID_TOKEN_INDICATING_MESSAGES = Arrays.asList(
            "invalid_grant",
            "Refresh token has expired",
            "Refresh token has been invalidated",
            "Refresh token is not valid",
            "Bad refresh token",
            "bad_request.client_mismatch",
            "Refresh token has been evicted"
    );

    @Override
    public void handle(final HttpStatusCodeException e) throws TokenInvalidException {
        if (shouldThrowTokenInvalidException(e)) {
            throw new TokenInvalidException("Token invalid, received status {}.");
        } else {
            throw e;
        }
    }

    private boolean shouldThrowTokenInvalidException(HttpStatusCodeException e) {
        return HttpStatus.UNAUTHORIZED.equals(e.getStatusCode()) ||
                HttpStatus.FORBIDDEN.equals(e.getStatusCode()) ||
                isBadRequestWithProperMessage(e);
    }

    private boolean isBadRequestWithProperMessage(HttpStatusCodeException e) {
        return HttpStatus.BAD_REQUEST.equals(e.getStatusCode()) && INVALID_TOKEN_INDICATING_MESSAGES.stream()
                .anyMatch(message -> e.getResponseBodyAsString().contains(message));
    }
}
