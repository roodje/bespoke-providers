package com.yolt.providers.openbanking.ais.vanquisgroup.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class VanquisGroupErrorHandlerV2 implements HttpErrorHandler {

    public static final VanquisGroupErrorHandlerV2 VANQUIS_GROUP_ERROR_HANDLER = new VanquisGroupErrorHandlerV2();

    @Override
    public void handle(final HttpStatusCodeException e) throws TokenInvalidException {
        if (HttpStatus.BAD_REQUEST.equals(e.getStatusCode()) && e.getResponseBodyAsString().contains("invalid grant error")) {
            throw new TokenInvalidException("Received error description: The client misses authorization for this request. User need to reconsent because of new license.");
        } else if (HttpStatus.BAD_REQUEST.equals(e.getStatusCode()) && e.getResponseBodyAsString().contains("invalid_grant")) {
            throw new TokenInvalidException("Unknown, invalid, or expired refresh token.");
        } else if (HttpStatus.FORBIDDEN.equals(e.getStatusCode()) || HttpStatus.UNAUTHORIZED.equals(e.getStatusCode())) {
            throw new TokenInvalidException("Obtained http status on call " + e.getStatusCode());
        } else {
            throw e;
        }
    }
}
