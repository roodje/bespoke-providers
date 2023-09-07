package com.yolt.providers.triodosbank.common.rest;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.triodosbank.common.auth.TriodosBankAuthenticationMeans;
import com.yolt.providers.triodosbank.common.config.TriodosBankBaseProperties;
import com.yolt.providers.triodosbank.common.model.AuthorisationResponse;
import com.yolt.providers.triodosbank.common.model.domain.SignatureData;
import com.yolt.providers.triodosbank.common.model.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;

import java.util.function.Consumer;

import static org.springframework.http.HttpMethod.*;

@RequiredArgsConstructor
@Slf4j
public class TriodosBankHttpClient {

    private static final String V1_CONSENT_AUTHORISATION = "/v1/consents/{consentId}/authorisations/{authorisationId}";
    private static final String V1_REGISTRATION_TOKEN = "/onboarding/v1";
    private static final String V1_CONSENT_CREATION_REQUEST = "/v1/consents";
    private static final String V1_CONSENT_STATUS = "/v1/consents/{consentId}/status";
    private static final String V1_ACCOUNTS = "/v1/accounts";

    private static final String INITIAL_ACCESS_TOKEN = "initial_access_token";
    private static final String SECTOR_IDENTIFIER_URI = "sector_identifier_uri";
    private static final String REDIRECT_URIS = "redirect_uris";
    private static final String GRANT_TYPE = "grant_type";
    private static final String CODE = "code";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String CODE_VERIFIER = "code_verifier";
    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String REFRESH_TOKEN = "refresh_token";

    private final RestOperations restOperations;
    private final TriodosBankAuthenticationMeans authMeans;
    private final TriodosBankHttpHeadersFactory headersFactory;
    private final TriodosBankHttpErrorHandler errorHandler;
    private final TriodosBankBaseProperties properties;
    private final Signer signer;

    public RegistrationTokenResponse getRegistrationToken() {
        HttpHeaders headers = headersFactory.createRegistrationTokenHeaders(getSignatureData());
        return exchange(V1_REGISTRATION_TOKEN, GET, new HttpEntity(headers), RegistrationTokenResponse.class);
    }

    public RegistrationResponse getRegistrationResponse(String registrationUrl,
                                                        RegistrationRequest request) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        request.getRedirectUris().forEach(s -> parameters.add(REDIRECT_URIS, s));
        parameters.add(INITIAL_ACCESS_TOKEN, request.getRegistrationToken());
        parameters.add(SECTOR_IDENTIFIER_URI, request.getSectorIdentifierUri());
        HttpHeaders headers = headersFactory.createRegistrationHeaders(getSignatureData(), request.getRegistrationToken());
        return exchange(registrationUrl, POST, new HttpEntity<>(parameters, headers), RegistrationResponse.class);
    }

    public ConsentCreationResponse getConsentResponse(ConsentCreationRequest request, String redirectUrl, String psuIpAddress) {
        HttpHeaders headers = headersFactory.createConsentCreationResponseHeaders(getSignatureData(), redirectUrl, psuIpAddress, request);
        return exchange(V1_CONSENT_CREATION_REQUEST, POST, new HttpEntity<>(request, headers), ConsentCreationResponse.class);
    }

    public ConsentStatusResponse getConsentStatus(String consentId) {
        HttpHeaders headers = headersFactory.createConsentStatusHeaders(getSignatureData());
        return exchange(V1_CONSENT_STATUS, GET, new HttpEntity(headers), ConsentStatusResponse.class, consentId);
    }

    public AuthorisationResponse getConsentAuthorisation(String consentId, String authorisationId, String psuIpAddress, String accessToken) {
        HttpHeaders headers = headersFactory.createAuthorisationHeaders(getSignatureData(), psuIpAddress, accessToken);
        return exchange(V1_CONSENT_AUTHORISATION, PUT, new HttpEntity(headers), AuthorisationResponse.class, consentId, authorisationId);
    }

    public TokenResponse getTokenResponse(String authorizationCode,
                                          String redirectUrl,
                                          String codeVerifier) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, AUTHORIZATION_CODE);
        body.add(CODE, authorizationCode);
        body.add(REDIRECT_URI, redirectUrl);
        body.add(CODE_VERIFIER, codeVerifier);

        HttpHeaders headers = headersFactory.createTokenHeaders(authMeans.getClientId(), authMeans.getClientSecret());
        return exchange(properties.getTokenUrl(), POST, new HttpEntity<>(body, headers), TokenResponse.class);
    }

    public TokenResponse getRefreshedToken(String refreshToken) throws TokenInvalidException {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, REFRESH_TOKEN);
        body.add(REFRESH_TOKEN, refreshToken);

        HttpHeaders headers = headersFactory.createTokenHeaders(authMeans.getClientId(), authMeans.getClientSecret());
        TokenResponse tokenResponse = null;
        try {
            tokenResponse = exchange(properties.getTokenUrl(), POST, new HttpEntity<>(body, headers), TokenResponse.class);
        } catch (HttpStatusCodeException e) {
            errorHandler.handleNon2xxResponseCode(e);
        }
        return tokenResponse;
    }

    public AccountsResponse getAccounts(String consentId, String token) throws TokenInvalidException {
        HttpHeaders headers = headersFactory.createFetchDataHeaders(getSignatureData(), consentId, token);
        return exchangeFetchData(V1_ACCOUNTS, new HttpEntity(headers), AccountsResponse.class);
    }

    public BalancesResponse getBalances(String consentId, String token, String balancesUrl) throws TokenInvalidException {
        HttpHeaders headers = headersFactory.createFetchDataHeaders(getSignatureData(), consentId, token);
        return exchangeFetchData(removeCountryPartIfExists(balancesUrl), new HttpEntity(headers), BalancesResponse.class);
    }

    public TransactionsResponse getTransactions(String consentId, String token, String transactionsUrl) throws TokenInvalidException {
        HttpHeaders headers = headersFactory.createFetchDataHeaders(getSignatureData(), consentId, token);
        return exchangeFetchData(removeCountryPartIfExists(transactionsUrl), new HttpEntity<>(headers), TransactionsResponse.class);
    }

    private String removeCountryPartIfExists(String url) {
        String countryPart = properties.getCountryPartFromBaseUrl();
        if (url.startsWith(countryPart)) {
            return url.replaceFirst(countryPart, "");
        }
        return url;
    }

    private SignatureData getSignatureData() {
        return authMeans.getSignatureData(signer);
    }

    private <T> T exchangeFetchData(String url, HttpEntity entity, Class<T> responseType, Object... uriVariables) throws TokenInvalidException {
        try {
            return exchange(url, GET, entity, responseType, uriVariables);
        } catch (HttpStatusCodeException e) {
            errorHandler.handleNon2xxResponseCode(e);
            throw e;
        }
    }

    private <T> T exchange(String url, HttpMethod method, HttpEntity entity, Class<T> responseType, Object... uriVariables) {
        return restOperations.exchange(url, method, entity, responseType, uriVariables).getBody();
    }
}
