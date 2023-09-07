package com.yolt.providers.starlingbank.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandler;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class StarlingBankHttpErrorHandler implements HttpErrorHandler {

    private final DefaultHttpErrorHandler defaultHttpErrorHandler;

    public StarlingBankHttpErrorHandler() {
        this.defaultHttpErrorHandler = new DefaultHttpErrorHandler();
    }

    @Override
    public void handle(HttpStatusCodeException e) throws TokenInvalidException {
        final HttpStatus status = e.getStatusCode();

        if (isInvalidRefreshToken(status, e.getResponseBodyAsString())) {
            throw new TokenInvalidException("Refresh token could not be verified, it could be invalid, expired or revoked: HTTP " + status.value());
        }
        defaultHttpErrorHandler.handle(e);
    }

    private boolean isInvalidRefreshToken(HttpStatus httpStatus, String errorResponse) {
        return BAD_REQUEST.equals(httpStatus) && (
                errorResponse.contains("refresh_token could not be verified, it could be invalid, expired or revoked")
                        || errorResponse.contains("{\"error\":\"invalid_client\",\"error_description\":\"Client not authorised to access token or authentication failed\"}")
        );
    }
}
