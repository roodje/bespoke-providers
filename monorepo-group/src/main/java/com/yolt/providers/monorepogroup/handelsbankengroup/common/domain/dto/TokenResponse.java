package com.yolt.providers.monorepogroup.handelsbankengroup.common.domain.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface TokenResponse {

    @JsonPath("$.access_token")
    String getAccessToken();

    @JsonPath("$.refresh_token")
    String getRefreshToken();

    @JsonPath("$.expires_in")
    Long getExpiresIn();

}
