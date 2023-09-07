package com.yolt.providers.redsys.common.util;

import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.ProviderHttpStatusException;
import com.yolt.providers.common.exception.TokenInvalidException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public final class ErrorHandlerUtil {

    private ErrorHandlerUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void handleNon2xxResponseInAuthorization(HttpStatusCodeException e, String psuIpAddress) throws TokenInvalidException {
        HttpStatus status = e.getStatusCode();
        final String errorMessage;
        switch (e.getStatusCode()) {
            case BAD_REQUEST:
                if (e.getResponseBodyAsString().contains("invalid_grant")) {
                    errorMessage = "Token is invalid or expired: HTTP " + status.value();
                    throw new TokenInvalidException(errorMessage);
                }
                errorMessage = "Request formed incorrectly: HTTP " + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
            case UNAUTHORIZED:
                errorMessage = "We are not authorized to call endpoint: HTTP " + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
            case FORBIDDEN:
                errorMessage = "Access to call is forbidden: HTTP " + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
            case INTERNAL_SERVER_ERROR:
                errorMessage = "Something went wrong on bank side: HTTP " + status.value();
                throw new TokenInvalidException(errorMessage);
            case TOO_MANY_REQUESTS:
                if (StringUtils.isEmpty(psuIpAddress)) {
                    throw new BackPressureRequestException(status.getReasonPhrase() + " " + status.value());
                } else {
                    return;
                }
            default:
                errorMessage = "Unknown exception: HTTP " + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
        }
    }

    public static void handleNon2xxResponseInFetchData(HttpStatusCodeException e, String psuIpAddress) throws TokenInvalidException {
        HttpStatus status = e.getStatusCode();
        final String errorMessage;
        switch (status) {
            case BAD_REQUEST:
                if (e.getResponseBodyAsString().contains("invalid_grant")) {
                    errorMessage = "Token is invalid or expired: HTTP " + status.value();
                    throw new TokenInvalidException(errorMessage);
                }
                errorMessage = "Request formed incorrectly: HTTP " + status.value();
                throw new ProviderHttpStatusException(errorMessage);
            case UNAUTHORIZED:
                errorMessage = "We are not authorized to call endpoint: HTTP " + status.value();
                throw new TokenInvalidException(errorMessage);
            case FORBIDDEN:
                errorMessage = "Access to call is forbidden: HTTP " + status.value();
                throw new TokenInvalidException(errorMessage);
            case INTERNAL_SERVER_ERROR:
                errorMessage = "Something went wrong on bank side: HTTP " + status.value();
                throw new TokenInvalidException(errorMessage);
            case TOO_MANY_REQUESTS:
                if (StringUtils.isEmpty(psuIpAddress)) {
                    throw new BackPressureRequestException(status.getReasonPhrase() + " " + status.value());
                } else {
                    return;
                }
            default:
                errorMessage = "Unknown exception: HTTP " + status.value();
                throw new GetAccessTokenFailedException(errorMessage);
        }
    }
}
