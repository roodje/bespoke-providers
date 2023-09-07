package com.yolt.providers.stet.generic.service.authorization.tool;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

public class DefaultAuthorizationCodeExtractor implements AuthorizationCodeExtractor {
    protected static final String ERROR_MESSAGE = "Cannot extract authorization code due to an error in callback URL. Error details: ";

    @Override
    public String extractAuthorizationCode(String redirectUrlPostedBackFromSite) {
        Map<String, String> queryParams = UriComponentsBuilder.fromUriString(redirectUrlPostedBackFromSite)
                .build()
                .getQueryParams()
                .toSingleValueMap();

        validateRedirect(queryParams);

        return queryParams.get(OAuth.CODE);
    }

    protected void validateRedirect(Map<String, String> queryParams) {
        String error = queryParams.get("error");
        if (StringUtils.isNotEmpty(error)) {
            throw new GetAccessTokenFailedException(ERROR_MESSAGE + error);
        }

        String code = queryParams.get(OAuth.CODE);
        if (StringUtils.isEmpty(code)) {
            throw new GetAccessTokenFailedException(ERROR_MESSAGE + "Authorization code not provided");
        }
    }
}
