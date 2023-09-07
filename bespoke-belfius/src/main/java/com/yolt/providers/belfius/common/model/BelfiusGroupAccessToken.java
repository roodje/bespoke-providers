package com.yolt.providers.belfius.common.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class BelfiusGroupAccessToken {

    @JsonAlias("access_token")
    private String accessToken;
    @JsonAlias("refresh_token")
    private String refreshToken;
    @JsonAlias("logical_id")
    private String logicalId;
    @JsonAlias("expires_in")
    private Long expiresIn;
}
