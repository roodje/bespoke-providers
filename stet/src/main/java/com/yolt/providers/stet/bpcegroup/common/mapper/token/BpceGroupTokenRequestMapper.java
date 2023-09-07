package com.yolt.providers.stet.bpcegroup.common.mapper.token;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.stet.generic.mapper.token.DefaultTokenRequestMapper;
import com.yolt.providers.stet.generic.service.authorization.request.AccessTokenRequest;
import com.yolt.providers.stet.generic.service.authorization.request.RefreshTokenRequest;
import org.springframework.util.MultiValueMap;

public class BpceGroupTokenRequestMapper extends DefaultTokenRequestMapper {

    @Override
    protected MultiValueMap<String, String> enhanceAccessTokenRequestBody(AccessTokenRequest request, MultiValueMap<String, String> body) {
        body.remove(OAuth.SCOPE);
        return body;
    }

    @Override
    protected MultiValueMap<String, String> enhanceRefreshTokenRequestBody(RefreshTokenRequest request, MultiValueMap<String, String> body) {
        body.remove(OAuth.SCOPE);
        return body;
    }
}
