package com.yolt.providers.openbanking.ais.amexgroup.common.service.restclient;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

public class AmexGroupRestClient extends DefaultRestClient {

    public AmexGroupRestClient(PaymentRequestSigner payloadSigner) {
        super(payloadSigner);
    }

    private HttpHeaders getAmexHeaders(final AccessMeans accessToken,
                                       final String clientId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken.getAccessToken());
        headers.add("x-amex-api-key", clientId);
        headers.add("x-client-id", clientId);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    @Override
    public <T> T postAccountAccessConsents(final HttpClient httpClient,
                                           final String exchangePath,
                                           final AccessMeans clientAccessToken,
                                           final DefaultAuthMeans authMeans,
                                           final Object body,
                                           final Class<T> responseType
    ) throws TokenInvalidException {
        return httpClient.exchange(exchangePath,
                HttpMethod.POST,
                new HttpEntity<>(body, getAmexHeaders(clientAccessToken, authMeans.getClientId())),
                ProviderClientEndpoints.GET_ACCOUNT_ACCESS_CONSENT,
                responseType,
                getErrorHandler()).getBody();
    }

    @Override
    public void deleteAccountAccessConsent(final HttpClient httpClient,
                                           final String exchangePath,
                                           final AccessMeans clientAccessToken,
                                           final String consentId,
                                           final DefaultAuthMeans authMeans) throws TokenInvalidException {
        httpClient.exchange(exchangePath + "/" + consentId,
                HttpMethod.DELETE,
                new HttpEntity<>(getAmexHeaders(clientAccessToken, authMeans.getClientId())),
                ProviderClientEndpoints.DELETE_ACCOUNT_ACCESS_CONSENT,
                Void.class);
    }

    @Override
    public <T> T fetchAccounts(final HttpClient httpClient,
                               final String currentPath,
                               final AccessMeans accessToken,
                               final String institutionId, //clientId in case of Amex
                               final Class<T> responseType) throws TokenInvalidException {
        return httpClient.exchange(currentPath,
                HttpMethod.GET,
                new HttpEntity<>(getAmexHeaders(accessToken, institutionId)),
                ProviderClientEndpoints.GET_ACCOUNTS,
                responseType,
                getErrorHandler()).getBody();
    }

    @Override
    public <T> T fetchTransactions(final HttpClient httpClient,
                                   final String currentPath,
                                   final AccessMeans accessToken,
                                   final String institutionId, //clientId in case of Amex
                                   final Class<T> responseType) throws TokenInvalidException {
        return httpClient.exchange(currentPath,
                HttpMethod.GET,
                new HttpEntity<>(getAmexHeaders(accessToken, institutionId)),
                ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID,
                responseType,
                getErrorHandler()).getBody();

    }

    @Override
    public <T> T fetchBalances(final HttpClient httpClient,
                               final String currentPath,
                               final AccessMeans accessToken,
                               final String institutionId, //clientId in case of Amex
                               final Class<T> responseType) throws TokenInvalidException {
        return httpClient.exchange(currentPath,
                HttpMethod.GET,
                new HttpEntity<>(getAmexHeaders(accessToken, institutionId)),
                ProviderClientEndpoints.GET_BALANCES_BY_ACCOUNT_ID,
                responseType,
                getErrorHandler()).getBody();
    }
}
