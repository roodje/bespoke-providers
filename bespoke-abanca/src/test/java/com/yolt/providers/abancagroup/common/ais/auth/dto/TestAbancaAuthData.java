package com.yolt.providers.abancagroup.common.ais.auth.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TestAbancaAuthData implements AbancaAuthData {

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
