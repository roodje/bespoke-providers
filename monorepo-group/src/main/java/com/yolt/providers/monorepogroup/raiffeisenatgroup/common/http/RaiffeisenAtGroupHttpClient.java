package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.*;
import org.springframework.util.MultiValueMap;

import java.time.Instant;

public interface RaiffeisenAtGroupHttpClient {
    Token createClientCredentialToken(final MultiValueMap<String, String> requestBody) throws TokenInvalidException;

    CreateConsentResponse createUserConsent(final String clientCredentialToken, final ConsentRequest consentRequest, final String redirectUri, final String psuIpAddress) throws TokenInvalidException;

    GetConsentResponse getConsentStatus(final String clientCredentialToken, final String consentId, final String psuIpAddress) throws TokenInvalidException;

    void deleteUserConsent(final String clientCredentialToken, final String externalConsentId, final String psuIpAddress) throws TokenInvalidException;

    AccountResponse fetchAccounts(final String clientAccessToken, final String consentId, final String psuIpAddress) throws TokenInvalidException;

    TransactionResponse fetchTransaction(final String resourceId, final String clientAccessToken, final String consentId, final String psuIpAddress, final Instant transactionsFetchStartTime) throws TokenInvalidException;

    TransactionResponse fetchTransaction(final String nextPageUrl, final String clientAccessToken, final String consentId, final String psuIpAddress) throws TokenInvalidException;

    RegistrationResponse register() throws TokenInvalidException;
}
