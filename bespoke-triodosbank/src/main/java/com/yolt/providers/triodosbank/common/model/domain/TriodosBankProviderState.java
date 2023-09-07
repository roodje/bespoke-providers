package com.yolt.providers.triodosbank.common.model.domain;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.triodosbank.common.model.http.ConsentCreationResponse;
import com.yolt.providers.triodosbank.common.model.http.TokenResponse;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TriodosBankProviderState {

    private String codeVerifier;
    private String consentId;
    private String authorisationId;
    private String accessToken;
    private String refreshToken;

    public TriodosBankProviderState(OAuth2ProofKeyCodeExchange codeExchange, ConsentCreationResponse consentResponse) {
        this(codeExchange, consentResponse.getConsentId(), consentResponse.getAuthorisationId());
    }

    public TriodosBankProviderState(OAuth2ProofKeyCodeExchange codeExchange, String consentId, String authorisationId) {
        this.codeVerifier = codeExchange.getCodeVerifier();
        this.consentId = consentId;
        this.authorisationId = authorisationId;
    }

    public void setTokens(TokenResponse response) throws TokenInvalidException {
        if (StringUtils.isEmpty(response.getAccessToken())) {
            throw new TokenInvalidException("Access token is missing");
        }
        accessToken = response.getAccessToken();
        if (StringUtils.isEmpty(response.getRefreshToken())) {
            throw new TokenInvalidException("Refresh token is missing");
        }
        refreshToken = response.getRefreshToken();
    }
}
