package com.yolt.providers.stet.generic.dto.token;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface TokenResponseDTO {

    @JsonPath("$.access_token")
    String getAccessToken();

    @JsonPath("$.client_id")
    String getClientId();

    @JsonPath("$.expires_in")
    long getExpiresIn();

    @JsonPath("$.refresh_token")
    String getRefreshToken();

    @JsonPath("$.token_type")
    String getTokenType();

    @JsonPath("$.user_id")
    String getUserId();

    @JsonPath("$.scope")
    String getScope();
}
