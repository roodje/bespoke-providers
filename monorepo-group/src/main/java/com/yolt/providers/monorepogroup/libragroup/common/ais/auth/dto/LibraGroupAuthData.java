package com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface LibraGroupAuthData {

    @JsonPath("$.access_token")
    String getAccessToken();

    @JsonPath("$.expires_in")
    Long getExpiresIn();

    @JsonPath("$.refresh_token")
    String getRefreshToken();

}