package com.yolt.providers.alpha.common.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.alpha.common.auth.AlphaSigner;
import com.yolt.providers.alpha.common.auth.dto.*;
import com.yolt.providers.alpha.common.config.AlphaProperties;
import com.yolt.providers.alpha.common.http.AlphaHttpClient;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.GetLoginInfoUrlFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;

import static com.yolt.providers.common.constants.OAuth.*;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@RequiredArgsConstructor
public class AlphaAuthenticationService implements AuthenticationService {

    private static final String REQUEST = "request";
    private static final String SCOPE_ACCOUNTS_SETUP = "account-info-setup";
    private static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    private static final String X_AB_BANK_ID = "x-ab-bank-id";
    private static final String X_JWS_SIGNATURE = "x-jws-signature";
    private static final String SCOPE_ACCOUNTS = "account-info";
    private static final String CLIENT_AUTHORIZATION_ENDPOINT = "/auth/authorize";
    private final AlphaProperties properties;
    private final ObjectMapper objectMapper;
    private final AlphaSigner alphaSigner;

    @Override
    public String getLoginInfo(final AlphaAuthMeans authMeans,
                               final AlphaHttpClient httpClient,
                               final String baseClientRedirectUrl,
                               final String state,
                               final Signer signer) throws TokenInvalidException {
        String clientId = authMeans.getClientId();
        String clientAccessToken = createClientToken(properties.getOAuthAuthorizationUrl(), clientId, authMeans.getClientSecret(), httpClient);
        String accountRequestId = createAccountRequestId(clientAccessToken, authMeans, httpClient, signer);
        MultiValueMap<String, String> httpQueryParams = new LinkedMultiValueMap<>();
        httpQueryParams.add(CLIENT_ID, clientId);
        httpQueryParams.add(RESPONSE_TYPE, CODE);
        httpQueryParams.add(SCOPE, SCOPE_ACCOUNTS);
        httpQueryParams.add(REDIRECT_URI, baseClientRedirectUrl);
        httpQueryParams.add(STATE, state);
        httpQueryParams.add(REQUEST, accountRequestId);
        return UriComponentsBuilder
                .fromHttpUrl(properties.getOAuthAuthorizationUrl() + CLIENT_AUTHORIZATION_ENDPOINT)
                .queryParams(httpQueryParams)
                .toUriString();
    }

    private String createClientToken(final String authUrl,
                                     final String clientId,
                                     final String clientSecret,
                                     final AlphaHttpClient httpClient) throws TokenInvalidException {
        HttpHeaders headers = getTokenHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        MultiValueMap<String, String> body = getClientTokenBody();
        AlphaToken response = httpClient.postForCCToken(authUrl, headers, body, ProviderClientEndpoints.CLIENT_CREDENTIALS_GRANT);
        return response.getAccessToken();
    }

    private MultiValueMap<String, String> getClientTokenBody() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, CLIENT_CREDENTIALS);
        body.add(SCOPE, SCOPE_ACCOUNTS_SETUP);
        return body;
    }

    private String createAccountRequestId(final String clientAccessToken,
                                          final AlphaAuthMeans authMeans,
                                          final AlphaHttpClient httpClient,
                                          final Signer signer) throws TokenInvalidException {
        String body = getAccountRequestEmptyBody();
        String signature = alphaSigner.getSignature(body, authMeans, signer);
        HttpHeaders headers = getAccountRequestHeaders(clientAccessToken, authMeans.getSubscriptionKey(), signature);
        AccountsRequestsResponse response = httpClient.postForAccountsRequests(headers, body, ProviderClientEndpoints.RETRIEVE_ACCOUNT_REQUEST_ID);
        return response.getAccountRequestId();
    }


    private String getAccountRequestEmptyBody() {
        AccountRequestPOSTRequestObject accountRequestObject = new AccountRequestPOSTRequestObject().risk(new Risk());
        try {
            return objectMapper.writeValueAsString(accountRequestObject);
        } catch (JsonProcessingException e) {
            throw new GetLoginInfoUrlFailedException("Could serialize account request object");
        }
    }

    private HttpHeaders getAccountRequestHeaders(final String clientAccessToken,
                                                 final String subscriptionKey,
                                                 final String signature) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(APPLICATION_JSON));
        headers.setBearerAuth(clientAccessToken);
        headers.set(OCP_APIM_SUBSCRIPTION_KEY, subscriptionKey);
        headers.set(X_AB_BANK_ID, properties.getBankId());
        headers.set(X_JWS_SIGNATURE, signature);
        return headers;
    }

    @Override
    public AlphaToken createNewAccessMeans(final AlphaAuthMeans authMeans,
                                           final AlphaHttpClient httpClient,
                                           final String redirectUrlPostedBackFromSite,
                                           final String baseClientRedirectUrl) throws TokenInvalidException {
        String authorizationCode = UriComponentsBuilder
                .fromUriString(redirectUrlPostedBackFromSite)
                .build()
                .getQueryParams()
                .toSingleValueMap()
                .get(CODE);
        MultiValueMap<String, String> body = getTokenBody(AUTHORIZATION_CODE, CODE, authorizationCode, authMeans);
        body.add(REDIRECT_URI, baseClientRedirectUrl);
        return httpClient.postForToken(properties.getOAuthAuthorizationUrl(), getTokenHeaders(), body, ProviderClientEndpoints.GET_ACCESS_TOKEN);
    }

    @Override
    public AlphaToken refreshAccessMeans(final AlphaAuthMeans authMeans,
                                         final AlphaHttpClient httpClient,
                                         final String refreshToken) throws TokenInvalidException {
        MultiValueMap<String, String> body = getTokenBody(REFRESH_TOKEN, REFRESH_TOKEN, refreshToken, authMeans);
        return httpClient.postForToken(properties.getOAuthAuthorizationUrl(), getTokenHeaders(), body, ProviderClientEndpoints.REFRESH_TOKEN);
    }

    private MultiValueMap<String, String> getTokenBody(final String grantType,
                                                       final String tokenType,
                                                       final String exchangedValue,
                                                       final AlphaAuthMeans authMeans) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(GRANT_TYPE, grantType);
        body.add(tokenType, exchangedValue);
        body.add(CLIENT_ID, authMeans.getClientId());
        body.add(CLIENT_SECRET, authMeans.getClientSecret());
        return body;
    }

    private HttpHeaders getTokenHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(APPLICATION_JSON));
        return headers;
    }
}
