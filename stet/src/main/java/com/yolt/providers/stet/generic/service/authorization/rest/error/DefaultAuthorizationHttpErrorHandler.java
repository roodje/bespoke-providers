package com.yolt.providers.stet.generic.service.authorization.rest.error;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.http.error.DefaultHttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

//TODO: C4PO-7223 Remove this class and migrate to new solution, when ticket will be completed
public class DefaultAuthorizationHttpErrorHandler extends DefaultHttpErrorHandler {

    @Override
    public void handle(HttpStatusCodeException e, ExecutionInfo executionInfo) throws TokenInvalidException {
        HttpStatus status = e.getStatusCode();
        switch (status) {
            case BAD_REQUEST:
                throw new GetAccessTokenFailedException("Request formed incorrectly: HTTP " + status.value());
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
