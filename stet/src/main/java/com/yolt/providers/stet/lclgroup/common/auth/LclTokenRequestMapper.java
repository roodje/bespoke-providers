package com.yolt.providers.stet.lclgroup.common.auth;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.stet.generic.mapper.token.DefaultTokenRequestMapper;
import com.yolt.providers.stet.generic.service.authorization.request.AccessTokenRequest;
import org.springframework.util.MultiValueMap;

public class LclTokenRequestMapper extends DefaultTokenRequestMapper {

    @Override
    protected MultiValueMap<String, String> enhanceAccessTokenRequestBody(final AccessTokenRequest request, final MultiValueMap<String, String> body) {
        body.remove(OAuth.SCOPE);
        return body;
    }
}
