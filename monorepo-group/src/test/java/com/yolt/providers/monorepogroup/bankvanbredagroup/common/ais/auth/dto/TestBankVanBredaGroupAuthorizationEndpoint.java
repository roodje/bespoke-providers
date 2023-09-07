package com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.dto;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TestBankVanBredaGroupAuthorizationEndpoint implements BankVanBredaGroupAuthorizationEndpoint {

    private final String authorizationUrl;

    @Override
    public String getAuthorizationUrl() {
        return authorizationUrl;
    }
}
