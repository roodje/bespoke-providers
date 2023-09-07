package com.yolt.providers.bunq.common.model;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface OauthAccessTokenResponse {
    @JsonPath("$.access_token")
    String getAccessToken();

    @JsonPath("$.token_type")
    String getTokenType();

    @JsonPath("$.state")
    String getState();
}
