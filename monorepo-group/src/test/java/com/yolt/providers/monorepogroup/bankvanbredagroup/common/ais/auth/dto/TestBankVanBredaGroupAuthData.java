package com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TestBankVanBredaGroupAuthData implements BankVanBredaGroupAuthData {

    private final String accessToken;
    private final Long expiresIn;
    private final String refreshToken;
    private final String scope;

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

    @Override
    public String getScope() {
        return scope;
    }
}
