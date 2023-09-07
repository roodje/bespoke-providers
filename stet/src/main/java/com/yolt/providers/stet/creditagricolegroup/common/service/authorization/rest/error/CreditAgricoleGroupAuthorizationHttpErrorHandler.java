package com.yolt.providers.stet.creditagricolegroup.common.service.authorization.rest.error;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.service.authorization.rest.error.DefaultAuthorizationHttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class CreditAgricoleGroupAuthorizationHttpErrorHandler extends DefaultAuthorizationHttpErrorHandler {

    @Override
    public void handle(HttpStatusCodeException e, ExecutionInfo executionInfo) throws TokenInvalidException {
        HttpStatus status = e.getStatusCode();
        if (HttpStatus.BAD_REQUEST.equals(status)) {
            String responseBody = e.getResponseBodyAsString();
            if (responseBody.contains("invalid_grant")) {
                throw new TokenInvalidException("Refresh token expired: HTTP " + status.value());
            }
            if (responseBody.contains("Compte inexistant ou bloque")) {
                throw new TokenInvalidException("Account non-existent or blocked: HTTP " + status.value());
            }
        }
        super.handle(e, executionInfo);
    }
}
