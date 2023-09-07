package com.yolt.providers.stet.cicgroup.common.service.authorization.tool;

import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;

import java.util.function.Supplier;

import static com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange.createRandomS256;

public class CicGroupCodeExchangeSupplier implements Supplier<OAuth2ProofKeyCodeExchange> {

    @Override
    public OAuth2ProofKeyCodeExchange get() {
        OAuth2ProofKeyCodeExchange codeExchange = createRandomS256();
        return new OAuth2ProofKeyCodeExchange(codeExchange.getCodeVerifier(), codeExchange.getCodeChallenge(), codeExchange.getCodeChallengeMethod());
    }
}
