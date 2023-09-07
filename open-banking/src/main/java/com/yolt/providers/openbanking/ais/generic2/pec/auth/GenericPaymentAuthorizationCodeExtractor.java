package com.yolt.providers.openbanking.ais.generic2.pec.auth;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.exception.PaymentCancelledException;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

public class GenericPaymentAuthorizationCodeExtractor {

    private static final String ERROR_QUERY_PARAM_NAME = "error";
    private static final String ACCESS_DENIED_ERROR_CODE = "access_denied";

    public String extractAuthorizationCode(String redirectUrlPostedBackFromSite) throws ConfirmationFailedException {
        var uriComponents = UriComponentsBuilder
                .fromUriString(redirectUrlPostedBackFromSite)
                .build();
        var queryAndFragmentParameters = uriComponents
                .getQueryParams()
                .toSingleValueMap();

        if (!StringUtils.isEmpty(uriComponents.getFragment())) {
            queryAndFragmentParameters.putAll(getMapFromFragmentString(uriComponents.getFragment()));
        }

        if (queryAndFragmentParameters.containsKey(ERROR_QUERY_PARAM_NAME)) {
            var error = queryAndFragmentParameters.get(ERROR_QUERY_PARAM_NAME);
            if (ACCESS_DENIED_ERROR_CODE.equals(error)) {
                throw new PaymentCancelledException("Got error in redirect URL: " + error);
            } else {
                throw new ConfirmationFailedException("Got error in redirect URL: " + error);
            }
        }

        return queryAndFragmentParameters.get(OAuth.CODE);
    }

    private Map<String, String> getMapFromFragmentString(String queryString) {
        var queryParams = queryString.split("&");
        Map<String, String> mappedQueryParams = new HashMap<>();
        for (var queryParam : queryParams) {
            var keyValue = queryParam.split("=");
            var value = keyValue.length == 2 ? keyValue[1] : null;
            mappedQueryParams.put(keyValue[0], value);
        }
        return mappedQueryParams;
    }
}
