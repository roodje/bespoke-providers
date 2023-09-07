package com.yolt.providers.n26.common.dto.token;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.time.Instant;
import java.util.Date;

@ProjectedPayload
public interface TokenResponse {

    @JsonPath("$.access_token")
    String getAccessToken();

    @JsonPath("$.refresh_token")
    String getRefreshToken();

    @JsonPath("$.expires_in")
    Long getExpiresIn();

}
