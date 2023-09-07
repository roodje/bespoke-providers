package com.yolt.providers.stet.bpcegroup.common.http.error;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.service.authorization.rest.error.DefaultAuthorizationHttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class BpceGroupAuthorizationErrorHandler extends DefaultAuthorizationHttpErrorHandler {

    @Override
    public void handle(HttpStatusCodeException e, ExecutionInfo executionInfo) throws TokenInvalidException {
        HttpStatus status = e.getStatusCode();
        switch (status) {
            case BAD_REQUEST: {
                if (e.getResponseBodyAsString().contains("invalid_grant")) {
                    throw new TokenInvalidException("Invalid grant, refresh token is invalid: HTTP " + status.value());
                }
                throw new GetAccessTokenFailedException("Request formed incorrectly: HTTP " + status.value());
            }
            case UNAUTHORIZED:
                throw new TokenInvalidException("We are not authorized to call endpoint: HTTP " + status.value());
            case FORBIDDEN:
                throw new TokenInvalidException("Access to call is forbidden: HTTP " + status.value());
            case INTERNAL_SERVER_ERROR:
                throw new GetAccessTokenFailedException("Something went wrong on bank side: HTTP " + status.value());
            default:
                throw new GetAccessTokenFailedException("Unknown exception: HTTP " + status.value());
        }
    }
}
