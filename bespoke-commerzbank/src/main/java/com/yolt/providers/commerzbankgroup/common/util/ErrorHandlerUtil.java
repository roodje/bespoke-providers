package com.yolt.providers.commerzbankgroup.common.util;

import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.common.exception.ProviderHttpStatusException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class ErrorHandlerUtil implements HttpErrorHandlerV2 {
    public ErrorHandlerUtil() {
    }

    public void handle(final HttpStatusCodeException e, Object additionalParameters) throws TokenInvalidException {
        HttpStatus status = e.getStatusCode();
        String errorMessage;
        switch (status) {
            case BAD_REQUEST:
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
            case TOO_MANY_REQUESTS:
                errorMessage = status.getReasonPhrase() + " " + status.value();
                throw new BackPressureRequestException(errorMessage);
            default:
                errorMessage = "Unknown exception: HTTP " + status.value();
                throw new ProviderHttpStatusException(errorMessage, e);
        }
    }
}
