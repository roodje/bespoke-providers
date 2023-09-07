package com.yolt.providers.commerzbankgroup.common.api.dto.authorization;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class CommerzbankGroupTokenResponse {

    @JsonProperty("access_token")
    String accessToken;

    @JsonProperty("token_type")
    String tokenType;

    @JsonProperty("expires_in")
    Integer expiresIn;

    @JsonProperty("refresh_token")
    String refreshToken;

    String scope;
}