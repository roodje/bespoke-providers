package com.yolt.providers.argentagroup.common.exception;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.ProviderHttpStatusException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Set;

public class TokensEndpointHttpErrorHandler implements HttpErrorHandler {

    private static final Set<String> EXPIRED_REFRESH_TOKEN_MESSAGES = Set.of(
            "invalid_grant",
            "refresh token is expired"
    );

    private static final Set<String> INVALID_AUTHORIZATION_CODE_MESSAGES = Set.of(
            "invalid_grant",
            "Invalid authorization code, authorization code not found"
    );

    @Override
    public void handle(final HttpStatusCodeException e) throws TokenInvalidException {
        final String errorMessage;
        final HttpStatus status = e.getStatusCode();
        switch (status) {
            case BAD_REQUEST:
                String errorBody = e.getResponseBodyAsString();
                boolean expiredRefreshTokenFlag = EXPIRED_REFRESH_TOKEN_MESSAGES.stream()
                        .allMatch(errorBody::contains);

                if (expiredRefreshTokenFlag) {
                    errorMessage = "Refresh token expired: HTTP " + status.value();
                    throw new TokenInvalidException(errorMessage, e);
                }

                boolean invalidAuthorizationCodeFlag = INVALID_AUTHORIZATION_CODE_MESSAGES.stream()
                        .allMatch(errorBody::contains);

                if (invalidAuthorizationCodeFlag) {
                    errorMessage = "Invalid authorization code: HTTP " + status.value();
                    throw new GetAccessTokenFailedException(errorMessage, e);
                }

                errorMessage = "Request formed incorrectly: HTTP " + status.value();
                throw new ProviderHttpStatusException(errorMessage, e);
            case UNAUTHORIZED:
                errorMessage = "We are not authorized to call endpoint: HTTP " + status.value();
                throw new ProviderHttpStatusException(errorMessage, e);
            case FORBIDDEN:
                errorMessage = "Access to call is forbidden: HTTP " + status.value();
                throw new ProviderHttpStatusException(errorMessage, e);
            case INTERNAL_SERVER_ERROR:
                errorMessage = "Something went wrong on bank side: HTTP " + status.value();
                throw new ProviderHttpStatusException(errorMessage, e);
            default:
                errorMessage = "Unknown exception: HTTP " + status.value();
                throw new ProviderHttpStatusException(errorMessage, e);
        }

    }
}
