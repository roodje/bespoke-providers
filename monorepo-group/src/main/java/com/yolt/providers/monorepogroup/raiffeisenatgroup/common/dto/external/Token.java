package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface Token {

    @JsonPath("$.access_token")
    String getAccessToken();

    @JsonPath("$.token_type")
    String getTokenType();

    @JsonPath("$.expires_in")
    String getTokenValidityTimeInSeconds();

}
