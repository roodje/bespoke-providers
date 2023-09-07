package com.yolt.providers.nutmeggroup.common.utils;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpErrorHandler {

    private static final String BAD_REQUEST_MESSAGE = "Request formed incorrectly: HTTP ";
    private static final String UNAUTHORIZED_MESSAGE = "We are not authorized to call endpoint: HTTP ";
    private static final String FORBIDDEN_MESSAGE = "Access to call is forbidden: HTTP ";
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Something went wrong on bank side: HTTP ";
    private static final String GENERIC_MESSAGE = "Unknown exception: HTTP ";

    public static void handleNon2xxResponseCode(final HttpStatus status) throws TokenInvalidException {
        final String errorMessage;
        switch (status) {
            case BAD_REQUEST:
                errorMessage = BAD_REQUEST_MESSAGE + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
            case UNAUTHORIZED:
                errorMessage = UNAUTHORIZED_MESSAGE + status.value();
                throw new TokenInvalidException(errorMessage);
            case FORBIDDEN:
                errorMessage = FORBIDDEN_MESSAGE + status.value();
                throw new TokenInvalidException(errorMessage);
            case INTERNAL_SERVER_ERROR:
                errorMessage = INTERNAL_SERVER_ERROR_MESSAGE + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
            default:
                errorMessage = GENERIC_MESSAGE + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
        }
    }
}
