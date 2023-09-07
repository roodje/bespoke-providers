package com.yolt.providers.bancatransilvania.common.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BancaTransilvaniaGroupProviderState {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String codeVerifier;
    private String consentId;
    private String redirectUri;
    private String accessToken;
    private String refreshToken;

    public BancaTransilvaniaGroupProviderState(OAuth2ProofKeyCodeExchange codeExchange, String clientId, String redirectUri) {
        this.codeVerifier = codeExchange.getCodeVerifier();
        this.consentId = clientId;
        this.redirectUri = redirectUri;
    }

    public void setTokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
