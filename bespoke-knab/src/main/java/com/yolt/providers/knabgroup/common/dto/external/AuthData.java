package com.yolt.providers.knabgroup.common.dto.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface AuthData {

    @JsonPath("$.access_token")
    String getAccessToken();

    @JsonPath("$.refresh_token")
    String getRefreshToken();

    @JsonPath("$.token_type")
    String getTokenType();

    @JsonPath("$.expires_in")
    Long getExpiresIn();

    @JsonPath("$.scope")
    String getScope();
}