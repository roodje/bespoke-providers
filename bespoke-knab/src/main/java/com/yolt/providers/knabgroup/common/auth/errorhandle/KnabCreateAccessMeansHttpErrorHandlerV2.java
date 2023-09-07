package com.yolt.providers.knabgroup.common.auth.errorhandle;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import static com.yolt.providers.knabgroup.common.http.HttpErrorMessages.*;

public class KnabCreateAccessMeansHttpErrorHandlerV2 implements HttpErrorHandler {

    @Override
    public void handle(final HttpStatusCodeException e) throws TokenInvalidException {
        final String errorMessage;
        final HttpStatus status = e.getStatusCode();
        switch (status) {
            case BAD_REQUEST:
                errorMessage = REQUEST_FORMED_INCORRECTLY_MESSAGE + status.value();
                break;
            case UNAUTHORIZED:
                errorMessage = NOT_AUTHORIZED_MESSAGE + status.value();
                break;
            case FORBIDDEN:
                errorMessage = ACCESS_FORBIDDEN_MESSAGE + status.value();
                break;
            case INTERNAL_SERVER_ERROR:
                errorMessage = ERROR_ON_THE_BANK_SIDE_MESSAGE + status.value();
                break;
            default:
                errorMessage = UNKNOWN_EXCEPTION_MESSAGE + status.value();
                break;
        }
        throw new GetAccessTokenFailedException(errorMessage);
    }
}