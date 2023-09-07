package com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BankVanBredaLoginUrlData {
    private final String loginUrl;
    private final String codeVerifier;
    private final String consentId;
}
