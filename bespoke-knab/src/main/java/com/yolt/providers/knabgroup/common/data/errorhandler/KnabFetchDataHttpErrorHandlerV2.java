package com.yolt.providers.knabgroup.common.data.errorhandler;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.knabgroup.common.exception.KnabGroupFetchDataException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import static com.yolt.providers.knabgroup.common.http.HttpErrorMessages.*;


public class KnabFetchDataHttpErrorHandlerV2 implements HttpErrorHandler {

    @Override
    public void handle(final HttpStatusCodeException e) throws TokenInvalidException {
        final String errorMessage;
        final HttpStatus status = e.getStatusCode();
        switch (status) {
            case BAD_REQUEST:
                errorMessage = REQUEST_FORMED_INCORRECTLY_MESSAGE + status.value();
                throw new KnabGroupFetchDataException(errorMessage, e);
            case UNAUTHORIZED:
                errorMessage = NOT_AUTHORIZED_MESSAGE + status.value();
                throw new TokenInvalidException(errorMessage);
            case FORBIDDEN:
                errorMessage = ACCESS_FORBIDDEN_MESSAGE + status.value();
                throw new TokenInvalidException(errorMessage);
            case INTERNAL_SERVER_ERROR:
                errorMessage = ERROR_ON_THE_BANK_SIDE_MESSAGE + status.value();
                throw new KnabGroupFetchDataException(errorMessage, e);
            default:
                errorMessage = UNKNOWN_EXCEPTION_MESSAGE + status.value();
                throw new KnabGroupFetchDataException(errorMessage, e);
        }
    }
}
