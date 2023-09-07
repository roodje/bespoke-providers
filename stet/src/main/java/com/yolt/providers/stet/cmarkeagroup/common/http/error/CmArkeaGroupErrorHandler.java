package com.yolt.providers.stet.cmarkeagroup.common.http.error;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.ProviderHttpStatusException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.service.authorization.rest.error.DefaultAuthorizationHttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class CmArkeaGroupErrorHandler extends DefaultAuthorizationHttpErrorHandler {

    @Override
    public void handle(HttpStatusCodeException e, ExecutionInfo executionInfo) throws TokenInvalidException {

        final String errorMessage;
        final HttpStatus status = e.getStatusCode();
        switch (status) {
            case BAD_REQUEST -> {
                if (e.getResponseBodyAsString().contains("OAGA")) {
                    throw new TokenInvalidException("Refresh token request has already used token: " + status);
                }
                errorMessage = "Request formed incorrectly: HTTP " + status.value();
                throw new ProviderHttpStatusException(errorMessage);
            }
            case UNAUTHORIZED -> {
                errorMessage = "We are not authorized to call endpoint: HTTP " + status.value();
                throw new TokenInvalidException(errorMessage);
            }
            case FORBIDDEN -> {
                errorMessage = "Access to call is forbidden: HTTP " + status.value();
                throw new TokenInvalidException(errorMessage);
            }
            case INTERNAL_SERVER_ERROR -> {
                errorMessage = "Something went wrong on bank side: HTTP " + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
            }
            default -> {
                errorMessage = "Unknown exception: HTTP " + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
            }
        }
    }
}
