package com.yolt.providers.stet.labanquepostalegroup.common.mapper.token;

import com.yolt.providers.stet.generic.service.authorization.request.AccessTokenRequest;
import com.yolt.providers.stet.generic.service.authorization.request.RefreshTokenRequest;
import com.yolt.providers.stet.generic.mapper.token.DefaultTokenRequestMapper;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.constants.OAuth.CLIENT_ID;

public class LaBanquePostaleGroupTokenRequestMapper extends DefaultTokenRequestMapper {

    @Override
    protected MultiValueMap<String, String> enhanceAccessTokenRequestBody(AccessTokenRequest request,
                                                                          MultiValueMap<String, String> body) {
        body.remove(CLIENT_ID);
        return body;
    }

    @Override
    protected MultiValueMap<String, String> enhanceRefreshTokenRequestBody(RefreshTokenRequest request,
                                                                           MultiValueMap<String, String> body) {
        body.remove(CLIENT_ID);
        return body;
    }
}
