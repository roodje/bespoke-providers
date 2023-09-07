package com.yolt.providers.knabgroup.common.auth.errorhandle;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class KnabPaymentAccessTokenErrorHandler implements HttpErrorHandler {

    @Override
    public void handle(final HttpStatusCodeException e) {
        final String errorMessage;
        final HttpStatus status = e.getStatusCode();
        switch (status) {
            case BAD_REQUEST:
                errorMessage = "Request formed incorrectly: HTTP " + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
            case UNAUTHORIZED:
                errorMessage = "We are not authorized to call endpoint: HTTP " + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
            case FORBIDDEN:
                errorMessage = "Access to call is forbidden: HTTP " + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
            case INTERNAL_SERVER_ERROR:
                errorMessage = "Something went wrong on ING side: HTTP " + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
            default:
                errorMessage = "Unknown exception: HTTP " + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
        }
    }
}
