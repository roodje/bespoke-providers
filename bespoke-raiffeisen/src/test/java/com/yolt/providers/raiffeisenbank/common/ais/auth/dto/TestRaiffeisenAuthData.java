package com.yolt.providers.raiffeisenbank.common.ais.auth.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TestRaiffeisenAuthData implements RaiffeisenAuthData {

    private final String accessToken;
    private final Long expiresIn;
    private final String refreshToken;

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public Long getExpiresIn() {
        return expiresIn;
    }

    @Override
    public String getRefreshToken() {
        return refreshToken;
    }
}
