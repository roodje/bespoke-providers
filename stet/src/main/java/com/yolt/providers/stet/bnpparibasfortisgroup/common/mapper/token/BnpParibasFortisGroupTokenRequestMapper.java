package com.yolt.providers.stet.bnpparibasfortisgroup.common.mapper.token;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.stet.generic.service.authorization.request.AccessTokenRequest;
import com.yolt.providers.stet.generic.mapper.token.DefaultTokenRequestMapper;
import org.springframework.util.MultiValueMap;

public class BnpParibasFortisGroupTokenRequestMapper extends DefaultTokenRequestMapper {

    @Override
    public MultiValueMap<String, String> mapAccessTokenRequest(AccessTokenRequest accessRequestDTO) {
        MultiValueMap<String, String> body = super.mapAccessTokenRequest(accessRequestDTO);
        body.add(OAuth.CLIENT_SECRET, accessRequestDTO.getAuthMeans().getClientSecret());
        return body;
    }
}
