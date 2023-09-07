package com.yolt.providers.axabanque.common.auth.http.client;

import com.yolt.providers.axabanque.common.model.external.AuthorizationResponse;
import com.yolt.providers.axabanque.common.model.external.ConsentResponse;
import com.yolt.providers.axabanque.common.model.external.Token;
import com.yolt.providers.common.exception.TokenInvalidException;

import java.time.LocalDate;

public interface AuthorizationHttpClient {
    ConsentResponse initiateConsent(String redirectUrl, String psuIpAddress, LocalDate validUntil, String xRequestId) throws TokenInvalidException;

    AuthorizationResponse initiateAuthorizationResource(ConsentResponse consentRequestDTO, String xRequestId) throws TokenInvalidException;

    Token createToken(String clientId, String redirectUri, String code, String codeVerifier) throws TokenInvalidException;

    Token refreshToken(String clientId, String redirectUri, String code, String codeVerifier, String refreshToken) throws TokenInvalidException;

    void deleteConsent(String xRequestId, String consentId) throws TokenInvalidException;
}
