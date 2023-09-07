package com.yolt.providers.triodosbank.common.rest;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.triodosbank.common.exception.ProviderHttpStatusException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

final class TriodosBankHttpErrorHandler {

    private static final String INVALID_TOKEN_INDICATING_MESSAGE = "Unknown refresh token";
    private static final String UNAUTHORIZED_CLIENT_INDICATING_MESSAGE = "Authorization code not issued to client";

    void handleNon2xxResponseCode(HttpStatusCodeException httpStatusCodeException) throws TokenInvalidException {
        final String errorMessage;
        HttpStatus status = httpStatusCodeException.getStatusCode();
        switch (status) {
            case BAD_REQUEST:
                if (isTokenUnrecognized(httpStatusCodeException.getResponseBodyAsString())) {
                    throw new TokenInvalidException("Token has not been recognized on the bank side");
                }
                if (isUnauthorizedClient(httpStatusCodeException.getResponseBodyAsString())) {
                    throw new TokenInvalidException("The authorization code doesn't match to the client");
                } else {
                    errorMessage = "Request formed incorrectly: HTTP " + status.value();
                    throw new ProviderHttpStatusException(errorMessage);
                }
            case UNAUTHORIZED:
                errorMessage = "We are not authorized to call endpoint: HTTP " + status.value();
                throw new TokenInvalidException(errorMessage);
            case FORBIDDEN:
                errorMessage = "Access to call is forbidden: HTTP " + status.value();
                throw new TokenInvalidException(errorMessage);
            case INTERNAL_SERVER_ERROR:
                errorMessage = "Something went wrong on bank side: HTTP " + status.value();
                throw new ProviderHttpStatusException(errorMessage);
            case TOO_MANY_REQUESTS:
                errorMessage = "Too many requests invoked: HTTP " + status.value();
                throw new ProviderHttpStatusException(errorMessage);
            default:
                errorMessage = "Unknown exception: HTTP " + status.value();
                throw new ProviderHttpStatusException(errorMessage);
        }
    }

    private boolean isTokenUnrecognized(String responseBody) {
        return StringUtils.isNotEmpty(responseBody) &&
                responseBody.contains(INVALID_TOKEN_INDICATING_MESSAGE);
    }

    private boolean isUnauthorizedClient(String responseBody) {
        return StringUtils.isNotEmpty(responseBody) &&
                responseBody.contains(UNAUTHORIZED_CLIENT_INDICATING_MESSAGE);
    }
}
