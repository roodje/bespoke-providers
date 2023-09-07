package com.yolt.providers.stet.cicgroup.common.http.error;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.service.authorization.rest.error.DefaultAuthorizationHttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class CicGroupHttpErrorHandler extends DefaultAuthorizationHttpErrorHandler {

    private static final String REFRESH_TOKEN_HAS_EXPIRED = "Refresh token has expired";
    private static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";

    @Override
    public void handle(HttpStatusCodeException e, ExecutionInfo executionInfo) throws TokenInvalidException {
        HttpStatus status = e.getStatusCode();
        String body = e.getResponseBodyAsString();
        switch (status) {
            case BAD_REQUEST:
                if (body.contains(REFRESH_TOKEN_HAS_EXPIRED)) {
                    throw new TokenInvalidException(REFRESH_TOKEN_HAS_EXPIRED + " HTTP status code " + status);
                } else if (body.contains(INVALID_REFRESH_TOKEN)) {
                    throw new TokenInvalidException(INVALID_REFRESH_TOKEN + " HTTP status code " + status);
                }
            default:
                super.handle(e, executionInfo);
        }
    }
}
