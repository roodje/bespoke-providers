package com.yolt.providers.stet.boursoramagroup.common.mapper.token;

import com.yolt.providers.stet.generic.mapper.token.TokenRequestMapper;
import com.yolt.providers.stet.generic.service.authorization.request.AccessTokenRequest;
import com.yolt.providers.stet.generic.service.authorization.request.RefreshTokenRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class BoursoramaGroupTokenRequestMapper implements TokenRequestMapper {

    @Override
    public <T extends AccessTokenRequest> MultiValueMap<String, String> mapAccessTokenRequest(T accessRequestDTO) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("clientId", accessRequestDTO.getAuthMeans().getClientId());
        body.add("authorizationCode", accessRequestDTO.getAuthorizationCode());
        body.add("grantType", "authorization_code");
        return body;
    }

    @Override
    public <T extends RefreshTokenRequest> MultiValueMap<String, String> mapRefreshTokenRequest(T refreshRequestDTO) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("refresh_token", refreshRequestDTO.getRefreshToken());
        return body;
    }
}