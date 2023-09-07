package com.yolt.providers.axabanque.common.model.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface Token {
    @JsonPath("$.access_token")
    String getAccessToken();

    @JsonPath("$.expires_in")
    Long getExpiresIn();

    @JsonPath("$.refresh_token")
    String getRefreshToken();

    @JsonPath("$.scope")
    String getScope();

    @JsonPath("$.token_type")
    String getTokenType();
}
