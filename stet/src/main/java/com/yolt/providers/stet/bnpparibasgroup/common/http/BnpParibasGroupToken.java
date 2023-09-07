package com.yolt.providers.stet.bnpparibasgroup.common.http;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class BnpParibasGroupToken {

    @JsonAlias("access_token")
    private String accessToken;

    @JsonAlias("client_id")
    private String clientId;

    @JsonAlias("expires_in")
    private long expiresIn;

    @JsonAlias("refresh_token")
    private String refreshToken;

    @JsonAlias("token_type")
    private String tokenType;

    @JsonAlias("user_id")
    private String userId;

    private String scope;
}
