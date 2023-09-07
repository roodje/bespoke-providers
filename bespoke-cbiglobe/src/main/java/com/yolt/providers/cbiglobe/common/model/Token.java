package com.yolt.providers.cbiglobe.common.model;

import lombok.Data;

import java.time.Clock;
import java.time.Instant;

@Data
public class Token {

    private String accessToken;
    private String tokenType;
    private Instant expiresIn;
    private String scope;

    public static Token from(TokenResponse tokenResponse, Clock clock) {
        if (tokenResponse == null) {
            throw new IllegalStateException("Token response is empty");
        }
        Token token = new Token();
        token.setAccessToken(tokenResponse.getAccessToken());
        token.setScope(tokenResponse.getScope());
        token.setTokenType(tokenResponse.getTokenType());
        token.setExpiresIn(Instant.now(clock).plusSeconds(Integer.parseInt(tokenResponse.getExpiresIn())));
        return token;
    }
}