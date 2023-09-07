package com.yolt.providers.stet.labanquepostalegroup.common.service.authorization.tool;

import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.stet.generic.service.authorization.tool.DefaultAuthorizationCodeExtractor;

import java.util.Map;

public class LaBanquePostaleGroupAuthorizationCodeExtractor extends DefaultAuthorizationCodeExtractor {

    @Override
    protected void validateRedirect(Map<String, String> queryParams) {
        String error = queryParams.get("error");
        if ("badredirect".equals(error)) {
            throw new GetAccessTokenFailedException("PSU has not subscribed or validated SCA. Error details: " + error);
        }
        super.validateRedirect(queryParams);
    }
}
