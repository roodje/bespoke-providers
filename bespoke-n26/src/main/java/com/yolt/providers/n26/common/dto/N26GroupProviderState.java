package com.yolt.providers.n26.common.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Instant;

@Data
@NoArgsConstructor
public class N26GroupProviderState {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String codeVerifier;
    private String consentId;
    private String redirectUri;
    private String requestId;
    private String accessToken;
    private String refreshToken;
    private Long consentGeneratedAt;

    public N26GroupProviderState(OAuth2ProofKeyCodeExchange codeExchange,
                                 String redirectUri,
                                 String requestId,
                                 Clock clock) {
        this.codeVerifier = codeExchange.getCodeVerifier();
        this.redirectUri = redirectUri;
        this.requestId = requestId;
        this.consentGeneratedAt = Instant.now(clock).toEpochMilli();
    }

    public void setTokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
