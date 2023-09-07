package com.yolt.providers.monorepogroup.chebancagroup.common.dto.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface CheBancaGroupToken {

    @JsonPath("$.data.access_token")
    String getAccessToken();

    @JsonPath("$.data.token_type")
    String getTokenType();

    @JsonPath("$.data.expires_in")
    Long getTokenValidityTimeInSeconds();

    @JsonPath("$.data.refresh_token")
    String getRefreshToken();

    @JsonPath("$.data.rt_expires_in")
    String getRefreshTokenValidityTimeInSeconds();

    @JsonPath("$.data.scope")
    String getScope();
}
