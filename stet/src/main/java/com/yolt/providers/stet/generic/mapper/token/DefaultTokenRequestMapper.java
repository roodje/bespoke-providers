package com.yolt.providers.stet.generic.mapper.token;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.stet.generic.service.authorization.request.AccessTokenRequest;
import com.yolt.providers.stet.generic.service.authorization.request.RefreshTokenRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

public class DefaultTokenRequestMapper implements TokenRequestMapper {

    protected static final String CODE_VERIFIER_KEY = "code_verifier";

    @Override
    public MultiValueMap<String, String> mapAccessTokenRequest(AccessTokenRequest accessRequestDTO) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.set(OAuth.GRANT_TYPE, OAuth.AUTHORIZATION_CODE);
        body.set(OAuth.CODE, accessRequestDTO.getAuthorizationCode());
        body.set(OAuth.SCOPE, accessRequestDTO.getAccessTokenScope().getValue());
        body.set(OAuth.CLIENT_ID, accessRequestDTO.getAuthMeans().getClientId());
        body.set(OAuth.REDIRECT_URI, accessRequestDTO.getRedirectUrl());

        String codeVerifier = accessRequestDTO.getProviderState().getCodeVerifier();
        if (!StringUtils.isEmpty(codeVerifier)) {
            body.set(CODE_VERIFIER_KEY, codeVerifier);
        }
        return enhanceAccessTokenRequestBody(accessRequestDTO, body);
    }

    @Override
    public MultiValueMap<String, String> mapRefreshTokenRequest(RefreshTokenRequest refreshRequestDTO) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.set(OAuth.GRANT_TYPE, OAuth.REFRESH_TOKEN);
        body.set(OAuth.SCOPE, refreshRequestDTO.getRefreshTokenScope().getValue());
        body.set(OAuth.REFRESH_TOKEN, refreshRequestDTO.getRefreshToken());
        body.set(OAuth.CLIENT_ID, refreshRequestDTO.getAuthMeans().getClientId());
        return enhanceRefreshTokenRequestBody(refreshRequestDTO, body);
    }

    protected MultiValueMap<String, String> enhanceAccessTokenRequestBody(AccessTokenRequest request, //NOSONAR It allows others to use it
                                                                          MultiValueMap<String, String> body) {
        return body;
    }

    protected MultiValueMap<String, String> enhanceRefreshTokenRequestBody(RefreshTokenRequest request, //NOSONAR It allows others to use it
                                                                           MultiValueMap<String, String> body) {
        return body;
    }
}
