package com.yolt.providers.stet.lclgroup.common.auth;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.stet.generic.service.authorization.tool.DefaultAuthorizationCodeExtractor;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class LclAuthorizationCodeExtractor extends DefaultAuthorizationCodeExtractor {

    @Override
    protected void validateRedirect(final Map<String, String> queryParams) {
        String code = queryParams.get(OAuth.CODE);
        if (StringUtils.isEmpty(code)) {
            throw new MissingDataException("Missing authorization code in redirect url query parameters");
        }
    }
}
