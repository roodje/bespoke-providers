package com.yolt.providers.stet.generic.mapper.token;

import com.yolt.providers.stet.generic.service.authorization.request.AccessTokenRequest;
import com.yolt.providers.stet.generic.service.authorization.request.RefreshTokenRequest;
import org.springframework.util.MultiValueMap;

public interface TokenRequestMapper {

    <T extends AccessTokenRequest> MultiValueMap<String, String> mapAccessTokenRequest(T accessRequestDTO);

    <T extends RefreshTokenRequest> MultiValueMap<String, String> mapRefreshTokenRequest(T refreshRequestDTO);
}