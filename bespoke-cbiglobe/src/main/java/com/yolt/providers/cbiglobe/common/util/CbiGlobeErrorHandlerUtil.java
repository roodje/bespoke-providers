package com.yolt.providers.cbiglobe.common.util;

import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CbiGlobeErrorHandlerUtil {

    public static void handleNon2xxResponseCodeFetchData(HttpStatus status, String psuIpAddress) throws TokenInvalidException, ProviderFetchDataException {
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
                errorMessage = "Something went wrong on CBI Globe side: HTTP " + status.value();
                throw new ProviderFetchDataException(errorMessage);
            case TOO_MANY_REQUESTS:
                if (StringUtils.isEmpty(psuIpAddress)) {
                    errorMessage = "Too many requests invoked without psu-ip-address present: HTTP " + status.value();
                    throw new BackPressureRequestException(errorMessage);
                } else {
                    errorMessage = "Too many requests invoked with psu-ip-address present: HTTP " + status.value();
                    throw new ProviderFetchDataException(errorMessage);
                }
            default:
                errorMessage = "Unknown exception: HTTP " + status.value();
                throw new ProviderFetchDataException(errorMessage);
        }
    }
}
