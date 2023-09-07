package com.yolt.providers.volksbank.common.rest;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandler;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import org.springframework.web.client.HttpStatusCodeException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class VolksbankHttpErrorHandler implements HttpErrorHandler {

    private final DefaultHttpErrorHandler defaultHttpErrorHandler;

    public VolksbankHttpErrorHandler() {
        defaultHttpErrorHandler = new DefaultHttpErrorHandler();
    }

    @Override
    public void handle(HttpStatusCodeException e) throws TokenInvalidException {
        // volksbank will fix it later, but now by unsupported_grant_type error they mean token is wrong and user should reconsent
        // volksbank return { "error":"invalid_request", "error_description":"Access or Refresh token expired" } in case of expired token
        if (BAD_REQUEST.equals(e.getStatusCode())) {
            String responseBodyAsString = e.getResponseBodyAsString();
            if (responseBodyAsString.contains("unsupported_grant_type")) {
                throw new TokenInvalidException("Error with status code: 400 unsupported_grant_type");
            } else if (responseBodyAsString.contains("Access or Refresh token expired")) {
                throw new TokenInvalidException("Error with status code: 400 Access or Refresh token expired");
            } else if (responseBodyAsString.contains("CONSENT_FAILED")) {
                throw new TokenInvalidException("Error with status code: 400 Consent call failed");
            }
        } else if (UNAUTHORIZED.equals(e.getStatusCode())) {
            throw new TokenInvalidException("Error with status code: 401 received during call");
        }
        defaultHttpErrorHandler.handle(e);
    }
}
