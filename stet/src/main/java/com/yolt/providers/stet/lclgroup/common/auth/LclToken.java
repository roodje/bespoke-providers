package com.yolt.providers.stet.lclgroup.common.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class LclToken {

    @JsonAlias("access_token")
    private String accessToken;

    @JsonAlias("refresh_token")
    private String refreshToken;

    @JsonAlias("token_type")
    private String tokenType;

    @JsonAlias("id_token")
    private String idToken;

    @JsonAlias("expires_in")
    private Long expiresIn;

}
