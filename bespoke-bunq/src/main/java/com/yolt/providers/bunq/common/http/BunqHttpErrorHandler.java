package com.yolt.providers.bunq.common.http;

import com.yolt.providers.common.exception.ProviderHttpStatusException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BunqHttpErrorHandler implements HttpErrorHandler {

    private static final String INVALID_TOKEN_RESPONSE = "User credentials are incorrect. Incorrect API key or IP address";

    public static final BunqHttpErrorHandler BUNQ_HTTP_ERROR_HANDLER = new BunqHttpErrorHandler();

    @Override
    public void handle(final HttpStatusCodeException e) throws TokenInvalidException {
        final String errorMessage;
        final HttpStatus status = e.getStatusCode();
        final String errorResponseBody = e.getResponseBodyAsString();
        switch (status) {
            case BAD_REQUEST:
                if (errorResponseBody.contains(INVALID_TOKEN_RESPONSE)) {
                    throw new TokenInvalidException(String.format("Token invalid, received status %s.", e.getStatusCode().toString()));
                }
                errorMessage = "Request formed incorrectly: HTTP " + status.value();
                throw new ProviderHttpStatusException(errorMessage, e);
            case UNAUTHORIZED:
                errorMessage = "We are not authorized to call endpoint: HTTP " + status.value();
                throw new TokenInvalidException(errorMessage, e);
            case FORBIDDEN:
                errorMessage = "Access to call is forbidden: HTTP " + status.value();
                throw new TokenInvalidException(errorMessage, e);
            case INTERNAL_SERVER_ERROR:
                errorMessage = "Something went wrong on bank side: HTTP " + status.value();
                throw new ProviderHttpStatusException(errorMessage, e);
            default:
                errorMessage = "Unknown exception: HTTP " + status.value();
                throw new ProviderHttpStatusException(errorMessage, e);
        }
    }

}
