package com.yolt.providers.fineco.errorhandling;

import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FetchDataErrorHandler {

    private static final String RESOURCE_UNKNOWN_CODE = "RESOURCE_UNKNOWN";

    public static void handleNon2xxResponseCodeFetchData(HttpStatusCodeException e, String psuIpAddress) throws TokenInvalidException, ProviderFetchDataException {
        final String errorMessage;
        final HttpStatus status = e.getStatusCode();
        switch (status) {
            case NOT_FOUND:
                // Fineco returns generic 404 exception with specific message in case of lack of card accounts.
                if (!e.getResponseBodyAsString().matches("(?s)(.*)Can't find(.*)accounts belonging to the specific consentId(.*)")) {
                    throw e;
                }
                break;
            case BAD_REQUEST:
                if (e.getResponseBodyAsString().contains(RESOURCE_UNKNOWN_CODE)) {
                    throw new TokenInvalidException("The account was closed");
                }
                errorMessage = "Request formed incorrectly: HTTP " + status.value();
                throw new ProviderFetchDataException(errorMessage);
            case UNAUTHORIZED:
                errorMessage = "We are not authorized to call endpoint: HTTP " + status.value();
                throw new TokenInvalidException(errorMessage);
            case FORBIDDEN:
                errorMessage = "Access to call is forbidden: HTTP " + status.value();
                throw new TokenInvalidException(errorMessage);
            case INTERNAL_SERVER_ERROR:
                errorMessage = "Something went wrong on Fineco side: HTTP " + status.value();
                throw new ProviderFetchDataException(errorMessage);
            case TOO_MANY_REQUESTS:
                if (StringUtils.isEmpty(psuIpAddress)) {
                    throw new BackPressureRequestException(status.getReasonPhrase() + " " + status.value());
                } else {
                    errorMessage = "Too many requests invoked with psu-ip-address present: HTTP " + status.value();
                }
                throw new ProviderFetchDataException(errorMessage);
            default:
                errorMessage = "Unknown exception: HTTP " + status.value();
                throw new ProviderFetchDataException(errorMessage);
        }
    }
}
