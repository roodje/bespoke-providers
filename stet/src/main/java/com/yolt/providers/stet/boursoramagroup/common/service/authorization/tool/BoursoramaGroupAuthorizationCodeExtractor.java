package com.yolt.providers.stet.boursoramagroup.common.service.authorization.tool;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.stet.generic.service.authorization.tool.DefaultAuthorizationCodeExtractor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

public class BoursoramaGroupAuthorizationCodeExtractor extends DefaultAuthorizationCodeExtractor {

    private static final String AUTHORIZATION_CODE_QUERY_PARAM = "authorization_code";

    @Override
    public String extractAuthorizationCode(String redirectUrlPostedBackFromSite) {
        Map<String, String> queryParams = UriComponentsBuilder.fromUriString(redirectUrlPostedBackFromSite)
                .build()
                .getQueryParams()
                .toSingleValueMap();

        validateRedirect(queryParams);
        return queryParams.get(AUTHORIZATION_CODE_QUERY_PARAM);
    }

    @Override
    protected void validateRedirect(Map<String, String> queryParams) {
        String error = queryParams.get("error");
        if (StringUtils.isNotEmpty(error)) {
            throw new GetAccessTokenFailedException(ERROR_MESSAGE + error);
        }

        String code = queryParams.get(AUTHORIZATION_CODE_QUERY_PARAM);
        if (StringUtils.isEmpty(code)) {
            throw new GetAccessTokenFailedException(ERROR_MESSAGE + "Authorization code not provided");
        }
    }
}