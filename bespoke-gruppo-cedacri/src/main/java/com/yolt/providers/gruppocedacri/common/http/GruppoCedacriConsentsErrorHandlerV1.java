package com.yolt.providers.gruppocedacri.common.http;

import com.yolt.providers.common.exception.ProviderHttpStatusException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class GruppoCedacriConsentsErrorHandlerV1 implements HttpErrorHandlerV2 {

    @Override
    public void handle(final HttpStatusCodeException e, Object additionalParameters) throws TokenInvalidException {
        HttpStatus status = e.getStatusCode();
        String errorMessage;
        switch (status) {
            case BAD_REQUEST:
                errorMessage = "Request formed incorrectly: HTTP " + status.value();
                throw new ProviderHttpStatusException(errorMessage, e);
            case UNAUTHORIZED:
                throw e;
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
