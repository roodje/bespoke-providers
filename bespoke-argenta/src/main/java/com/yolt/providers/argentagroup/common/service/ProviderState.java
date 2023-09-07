package com.yolt.providers.argentagroup.common.service;

import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProviderState {

    private String consentId;
    private OAuth2ProofKeyCodeExchange proofKeyCodeExchange;

}
