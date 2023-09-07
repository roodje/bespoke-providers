package com.yolt.providers.ing.common.auth;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface IngAuthData {

    @JsonPath("$.access_token")
    String getAccessToken();

    @JsonPath("$.refresh_token")
    String getRefreshToken();

    @JsonPath("$.client_id")
    String getClientId();

    @JsonPath("$.token_type")
    String getTokenType();

    @JsonPath("$.expires_in")
    Long getExpiresIn();

    @JsonPath("$.refresh_token_expires_in")
    Long getRefreshTokenExpiresIn();

    @JsonPath("$.scope")
    String getScope();
}