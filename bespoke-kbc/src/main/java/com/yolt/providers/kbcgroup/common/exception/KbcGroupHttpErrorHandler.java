package com.yolt.providers.kbcgroup.common.exception;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KbcGroupHttpErrorHandler {

    public static void handleNon2xxResponseCodeForTokenExchange(HttpStatus status) throws TokenInvalidException {
        final String errorMessage;
        switch (status) {
            case BAD_REQUEST:
                errorMessage = "Request formed incorrectly: HTTP " + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
            case UNAUTHORIZED:
                errorMessage = "We are not authorized to call endpoint: HTTP " + status.value();
                throw new TokenInvalidException(errorMessage);
            case FORBIDDEN:
                errorMessage = "Access to call is forbidden: HTTP " + status.value();
                throw new TokenInvalidException(errorMessage);
            case INTERNAL_SERVER_ERROR:
                errorMessage = "Something went wrong on bank side: HTTP " + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
            default:
                errorMessage = "Unknown exception: HTTP " + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
        }
    }

    public static void handleNon2xxResponseCodeForFetchData(HttpStatus status) throws ProviderFetchDataException, TokenInvalidException {
        final String errorMessage;
        switch (status) {
            case BAD_REQUEST:
                errorMessage = "Request formed incorrectly: HTTP " + status.value();
                throw new ProviderFetchDataException(errorMessage);
            case UNAUTHORIZED:
                errorMessage = "We are not authorized to call endpoint: HTTP " + status.value();
                throw new TokenInvalidException(errorMessage);
            case FORBIDDEN:
                errorMessage = "Access to call is forbidden: HTTP " + status.value();
                throw new TokenInvalidException(errorMessage);
            case INTERNAL_SERVER_ERROR:
                errorMessage = "Something went wrong on bank side: HTTP " + status.value();
                throw new ProviderFetchDataException(errorMessage);
            default:
                errorMessage = "Unknown exception: HTTP " + status.value();
                throw new ProviderFetchDataException(errorMessage);
        }
    }
}