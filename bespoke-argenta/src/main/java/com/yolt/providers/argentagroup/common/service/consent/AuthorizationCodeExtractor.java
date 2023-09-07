package com.yolt.providers.argentagroup.common.service.consent;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.MissingDataException;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

public class AuthorizationCodeExtractor {

    public String extractFromRedirectUrl(final String redirectUrl) {
        final UriComponents uriComponents = UriComponentsBuilder
                .fromUriString(redirectUrl)
                .build();
        final Map<String, String> queryAndFragmentParameters = uriComponents
                .getQueryParams()
                .toSingleValueMap();

        if (uriComponents.getFragment() != null && !uriComponents.getFragment().isEmpty()) {
            queryAndFragmentParameters.putAll(getMapFromFragmentString(uriComponents.getFragment()));
        }

        final String error = queryAndFragmentParameters.get("error");
        if (!StringUtils.isEmpty(error)) {
            throw new GetAccessTokenFailedException("Got error in callback URL. Login failed. Redirect URL: " + redirectUrl);
        }

        final String authorizationCode = queryAndFragmentParameters.get("code");
        if (StringUtils.isEmpty(authorizationCode)) {
            throw new MissingDataException("Missing data for key code.");
        }
        return authorizationCode;
    }

    private Map<String, String> getMapFromFragmentString(String queryString) {
        String[] queryParams = queryString.split("&");
        Map<String, String> mappedQueryParams = new HashMap<>();
        for (String queryParam : queryParams) {
            String[] keyValue = queryParam.split("=");
            String value = keyValue.length == 2 ? keyValue[1] : null;
            mappedQueryParams.put(keyValue[0], value);
        }
        return mappedQueryParams;
    }
}
