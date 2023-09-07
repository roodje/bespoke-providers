package com.yolt.providers.abnamrogroup.common.pis.pec.submit;

import com.yolt.providers.abnamrogroup.common.pis.pec.exception.AbnAmroConsentCancelledException;
import com.yolt.providers.common.exception.MissingDataException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

public class AbnAmroAuthorizationCodeExtractor {

    private static final String CODE = "code";
    private static final String ERROR_KEY = "error";

    public String extractAuthorizationCode(String redirectUrlPostedBackFromSite) {
        var queryParameters = UriComponentsBuilder
                .fromUriString(redirectUrlPostedBackFromSite)
                .build()
                .getQueryParams()
                .toSingleValueMap();

        checkForErrorCode(queryParameters);
        verifyAuthorizationCodeInRedirectQueryParams(queryParameters);
        return queryParameters.get(CODE);
    }

    private void checkForErrorCode(Map<String, String> queryParameters) {
        if (queryParameters.containsKey(ERROR_KEY)) {
            throw new AbnAmroConsentCancelledException("The user cancelled the consent process.");
        }
    }

    private void verifyAuthorizationCodeInRedirectQueryParams(Map<String, String> queryParameters) {
        if (!queryParameters.containsKey(CODE)) {
            throw new MissingDataException("Missing data for key code.");
        }
    }
}
