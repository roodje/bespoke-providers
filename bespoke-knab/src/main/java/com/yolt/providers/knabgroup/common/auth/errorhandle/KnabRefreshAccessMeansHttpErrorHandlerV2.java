package com.yolt.providers.knabgroup.common.auth.errorhandle;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import static com.yolt.providers.knabgroup.common.http.HttpErrorMessages.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class KnabRefreshAccessMeansHttpErrorHandlerV2 implements HttpErrorHandler {

    @Override
    public void handle(final HttpStatusCodeException e) throws TokenInvalidException {
        final String errorMessage;
        final HttpStatus status = e.getStatusCode();
        if (isInvalidRefreshToken(status, e.getResponseBodyAsString())) {
            throw new TokenInvalidException("Invalid grant: HTTP " + status.value());
        }
        switch (status) {
            case BAD_REQUEST:
                errorMessage = REQUEST_FORMED_INCORRECTLY_MESSAGE + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
            case UNAUTHORIZED:
                errorMessage = NOT_AUTHORIZED_MESSAGE + status.value();
                throw new TokenInvalidException(errorMessage);
            case FORBIDDEN:
                errorMessage = ACCESS_FORBIDDEN_MESSAGE + status.value();
                throw new TokenInvalidException(errorMessage);
            case INTERNAL_SERVER_ERROR:
                errorMessage = ERROR_ON_THE_BANK_SIDE_MESSAGE + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
            default:
                errorMessage = UNKNOWN_EXCEPTION_MESSAGE + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
        }
    }

    private boolean isInvalidRefreshToken(HttpStatus httpStatus, String errorResponse) {
        return BAD_REQUEST.equals(httpStatus) && errorResponse.contains("invalid_grant");
    }
}