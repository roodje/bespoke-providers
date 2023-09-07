package com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.dto;

import lombok.Getter;

@Getter
public class TestBankVanBredaGroupTokens extends BankVanBredaGroupTokens {
    private final String accessToken;
    private final Long expiryTimestamp;
    private final String refreshToken;

    public TestBankVanBredaGroupTokens(String accessToken, Long expiryTimestamp, String refreshToken) {
        this.accessToken = accessToken;
        this.expiryTimestamp = expiryTimestamp;
        this.refreshToken = refreshToken;
    }
}