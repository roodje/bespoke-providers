package com.yolt.providers.stet.bnpparibasgroup.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.service.authorization.rest.error.DefaultAuthorizationHttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class BnpParibasGroupErrorHandler extends DefaultAuthorizationHttpErrorHandler {

    @Override
    public void handle(HttpStatusCodeException e, ExecutionInfo executionInfo) throws TokenInvalidException {
        HttpStatus errorCode = e.getStatusCode();
        if (HttpStatus.FORBIDDEN.equals(errorCode) || HttpStatus.UNAUTHORIZED.equals(errorCode)
                || (HttpStatus.BAD_REQUEST.equals(errorCode) && e.getResponseBodyAsString().contains("invalid_grant"))) {
            throw new TokenInvalidException("Http error status code: " + errorCode);
        }
    }
}
