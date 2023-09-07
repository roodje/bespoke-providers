package com.yolt.providers.volksbank.common.rest;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.volksbank.common.auth.VolksbankAuthenticationMeans;
import com.yolt.providers.volksbank.common.model.VolksbankAccessTokenResponse;
import com.yolt.providers.volksbank.common.util.HttpUtils;
import com.yolt.providers.volksbank.dto.v1_1.*;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
public class VolksbankHttpClientV4 extends DefaultHttpClient {

    private final HttpErrorHandler errorHandler;

    private static final String CONSENT_ID_HEADER = "Consent-ID";
    private static final String VOLKSBANK_TRANSACTIONS_LIMIT = "2000";

    private static final String CONSENT_URL = "/v1/consents";
    private static final String ACCOUNT_URL = "/v1.1/accounts";
    private static final String TOKEN_URL_TEMPLATE = "/v1/token?grant_type=authorization_code&code={code}&redirect_uri={redirect_uri}";
    private static final String REFRESH_TOKEN_URL_TEMPLATE = "/v1/token?grant_type=refresh_token&refresh_token={refresh_token}&redirect_uri={redirect_uri}";
    private static final String TRANSACTION_URL_TEMPLATE = "/v1.1/accounts/{accountid}/transactions?bookingStatus={bookingStatus}&dateFrom={dateFrom}&limit={limit}";
    private static final String BALANCE_URL_TEMPLATE = "/v1.1/accounts/{accountid}/balances";

    public VolksbankHttpClientV4(MeterRegistry registry, RestTemplate restTemplate, String provider, HttpErrorHandler errorHandler) {
        super(registry, restTemplate, provider);
        this.errorHandler = errorHandler;
    }

    public ConsentInitiationResponse generateConsentUrl(final VolksbankAuthenticationMeans authenticationMeans,
                                                        final InitiateConsentRequest consentObject) throws TokenInvalidException {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, authenticationMeans.getClientId());
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        return call(
                CONSENT_URL,
                HttpMethod.POST,
                new HttpEntity<>(consentObject, headers),
                ProviderClientEndpoints.GET_ACCOUNT_ACCESS_CONSENT,
                ConsentInitiationResponse.class);
    }

    public VolksbankAccessTokenResponse getAccessToken(final VolksbankAuthenticationMeans authenticationMeans,
                                                       final String redirectUrl,
                                                       final String authorizationCode) throws TokenInvalidException {
        return call(
                TOKEN_URL_TEMPLATE,
                HttpMethod.POST,
                new HttpEntity<>(createTokenHeader(authenticationMeans)),
                ProviderClientEndpoints.GET_ACCESS_TOKEN,
                VolksbankAccessTokenResponse.class,
                authorizationCode, redirectUrl);
    }

    public VolksbankAccessTokenResponse getNewAccessTokenUsingRefreshToken(final VolksbankAuthenticationMeans authenticationMeans,
                                                                           final String redirectUrl,
                                                                           final String refreshToken) throws TokenInvalidException {
        return call(
                REFRESH_TOKEN_URL_TEMPLATE,
                HttpMethod.POST,
                new HttpEntity<>(createTokenHeader(authenticationMeans)),
                ProviderClientEndpoints.REFRESH_TOKEN,
                VolksbankAccessTokenResponse.class,
                refreshToken, redirectUrl);
    }

    public AccountResponse getAllUserAccounts(final String userAccessToken, final String consentId) throws TokenInvalidException {
        return call(
                ACCOUNT_URL,
                HttpMethod.GET,
                new HttpEntity<>(createDataFetchHeader(userAccessToken, consentId)),
                ProviderClientEndpoints.GET_ACCOUNTS,
                AccountResponse.class);
    }

    public BalanceResponse getBalanceForAccount(final String userAccessToken,
                                                final String consentId,
                                                final String resourceId) throws TokenInvalidException {
        return call(
                BALANCE_URL_TEMPLATE,
                HttpMethod.GET,
                new HttpEntity<>(createDataFetchHeader(userAccessToken, consentId)),
                ProviderClientEndpoints.GET_BALANCES_BY_ACCOUNT_ID,
                BalanceResponse.class,
                resourceId);
    }

    public TransactionResponse getUserTransactionForGivenAccount(final String userAccessToken,
                                                                 final String consentId,
                                                                 final String resourceId,
                                                                 final Instant transactionsFetchStartTime) throws TokenInvalidException {
        return call(
                TRANSACTION_URL_TEMPLATE,
                HttpMethod.GET,
                new HttpEntity<>(createDataFetchHeader(userAccessToken, consentId)),
                ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID,
                TransactionResponse.class,
                resourceId,
                "booked", //de Volksbank only delivers account information on booked transactions
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        .format(OffsetDateTime.ofInstant(transactionsFetchStartTime, ZoneOffset.UTC)),
                VOLKSBANK_TRANSACTIONS_LIMIT);
    }

    public TransactionResponse getUserTransactionForGivenAccount(final String userAccessToken,
                                                                 final String consentId,
                                                                 final String path) throws TokenInvalidException {
        return call(
                path,
                HttpMethod.GET,
                new HttpEntity<>(createDataFetchHeader(userAccessToken, consentId)),
                ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID,
                TransactionResponse.class);
    }

    private <T> T call(String path, HttpMethod method, HttpEntity request, String prometeusPath, Class<T> responseClass, String... uriVariables) throws TokenInvalidException {
        return exchange(path, method, request, prometeusPath, responseClass, errorHandler, uriVariables).getBody();
    }

    private HttpHeaders createTokenHeader(final VolksbankAuthenticationMeans authenticationMeans) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, HttpUtils.basicCredentials(authenticationMeans.getClientId(), authenticationMeans.getClientSecret()));
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        return headers;
    }

    private HttpHeaders createDataFetchHeader(final String userAccessToken,
                                              final String consentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(CONSENT_ID_HEADER, consentId);
        headers.setBearerAuth(userAccessToken);

        return headers;
    }
}