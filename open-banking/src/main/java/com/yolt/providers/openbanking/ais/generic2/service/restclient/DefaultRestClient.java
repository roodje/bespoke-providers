package com.yolt.providers.openbanking.ais.generic2.service.restclient;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpErrorHandler;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.UUID;

//TODO: Split this restTemplate into service type independent restClients C4PO-6078
@RequiredArgsConstructor
public class DefaultRestClient implements RestClient {
    // This signer is used only in payments flow it does not affect ais - should be separated from ais in C4PO-6078
    protected final PaymentRequestSigner payloadSigner;

    protected HttpHeaders getHeaders(final AccessMeans clientAccessToken,
                                     final String institutionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(clientAccessToken.getAccessToken());
        headers.add(HttpExtraHeaders.FINANCIAL_ID_HEADER_NAME, institutionId);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    protected HttpHeaders getPaymentHttpHeaders(final AccessMeans clientAccessToken,
                                                final DefaultAuthMeans authMeans,
                                                final Object requestBody,
                                                final Signer signer) {
        HttpHeaders headers = getHeaders(clientAccessToken, authMeans.getInstitutionId());
        headers.add(HttpExtraHeaders.IDEMPOTENT_KEY, UUID.randomUUID().toString());
        headers.add(HttpExtraHeaders.SIGNATURE_HEADER_NAME, payloadSigner.createRequestSignature(requestBody, authMeans, signer));
        return headers;
    }

    protected HttpErrorHandler getErrorHandler() {
        return DefaultHttpErrorHandler.DEFAULT_HTTP_ERROR_HANDLER;
    }

    @Override
    public <T> T postAccountAccessConsents(final HttpClient httpClient,
                                           final String exchangePath,
                                           final AccessMeans clientAccessToken,
                                           final DefaultAuthMeans authMeans,
                                           final Object body,
                                           final Class<T> responseType) throws TokenInvalidException {
        return httpClient.exchange(exchangePath,
                HttpMethod.POST,
                new HttpEntity<>(body, getHeaders(clientAccessToken, authMeans.getInstitutionId())),
                ProviderClientEndpoints.GET_ACCOUNT_ACCESS_CONSENT,
                responseType,
                getErrorHandler()).getBody();
    }

    @Override
    public <T> T getAccountAccessConsent(final HttpClient httpClient,
                                         final String exchangePath,
                                         final AccessMeans clientAccessToken,
                                         final DefaultAuthMeans authMeans,
                                         final Class<T> responseType) throws TokenInvalidException {
        return httpClient.exchange(exchangePath,
                HttpMethod.GET,
                new HttpEntity<>(getHeaders(clientAccessToken, authMeans.getInstitutionId())),
                ProviderClientEndpoints.RETRIEVE_ACCOUNT_ACCESS_CONSENT,
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
                new HttpEntity<>(getHeaders(clientAccessToken, authMeans.getInstitutionId())),
                ProviderClientEndpoints.DELETE_ACCOUNT_ACCESS_CONSENT,
                Void.class);
    }

    @Override
    public <T> T fetchAccounts(final HttpClient httpClient,
                               final String currentPath,
                               final AccessMeans accessToken,
                               final String institutionId,
                               final Class<T> responseType) throws TokenInvalidException {
        return httpClient.exchange(currentPath,
                HttpMethod.GET,
                new HttpEntity<>(getHeaders(accessToken, institutionId)),
                ProviderClientEndpoints.GET_ACCOUNTS,
                responseType,
                getErrorHandler()).getBody();
    }

    @Override
    public <T> T fetchTransactions(final HttpClient httpClient,
                                   final String currentPath,
                                   final AccessMeans accessToken,
                                   final String institutionId,
                                   final Class<T> responseType) throws TokenInvalidException {
        return httpClient.exchange(currentPath,
                HttpMethod.GET,
                new HttpEntity<>(getHeaders(accessToken, institutionId)),
                ProviderClientEndpoints.GET_TRANSACTIONS_BY_ACCOUNT_ID,
                responseType,
                getErrorHandler()).getBody();
    }

    @Override
    public <T> T fetchDirectDebits(final HttpClient httpClient,
                                   final String currentPath,
                                   final AccessMeans accessToken,
                                   final String institutionId,
                                   final Class<T> responseType) throws TokenInvalidException {
        return httpClient.exchange(currentPath,
                HttpMethod.GET,
                new HttpEntity<>(getHeaders(accessToken, institutionId)),
                ProviderClientEndpoints.GET_DIRECT_DEBITS_BY_ACCOUNT_ID,
                responseType,
                getErrorHandler()).getBody();
    }

    @Override
    public <T> T fetchStandingOrders(final HttpClient httpClient,
                                     final String currentPath,
                                     final AccessMeans accessToken,
                                     final String institutionId,
                                     final Class<T> responseType) throws TokenInvalidException {
        return httpClient.exchange(currentPath,
                HttpMethod.GET,
                new HttpEntity<>(getHeaders(accessToken, institutionId)),
                ProviderClientEndpoints.GET_STANDING_ORDERS_BY_ACCOUNT_ID,
                responseType,
                getErrorHandler()).getBody();
    }

    @Override
    public <T> T fetchBalances(final HttpClient httpClient,
                               final String currentPath,
                               final AccessMeans accessToken,
                               final String institutionId,
                               final Class<T> responseType) throws TokenInvalidException {
        return httpClient.exchange(currentPath,
                HttpMethod.GET,
                new HttpEntity<>(getHeaders(accessToken, institutionId)),
                ProviderClientEndpoints.GET_BALANCES_BY_ACCOUNT_ID,
                responseType,
                getErrorHandler()).getBody();
    }

    @Override
    public <T> T createPayment(final HttpClient httpClient,
                               final String exchangePath,
                               final AccessMeans clientAccessToken,
                               final DefaultAuthMeans authMeans,
                               final Object requestBody,
                               final Class<T> responseType,
                               final Signer signer) throws TokenInvalidException {
        HttpHeaders headers = getPaymentHttpHeaders(clientAccessToken, authMeans, requestBody, signer);

        return httpClient.exchange(exchangePath,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                ProviderClientEndpoints.INITIATE_PAYMENT,
                responseType,
                getErrorHandler()).getBody();
    }

    @Override
    public <T> T submitPayment(final HttpClient httpClient,
                               final String exchangePath,
                               final AccessMeans userAccessToken,
                               final DefaultAuthMeans authMeans,
                               final Object requestBody,
                               final Class<T> responseType,
                               final Signer signer) throws TokenInvalidException {
        HttpHeaders headers = getPaymentHttpHeaders(userAccessToken, authMeans, requestBody, signer);

        return httpClient.exchange(exchangePath,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                ProviderClientEndpoints.SUBMIT_PAYMENT,
                responseType,
                getErrorHandler()).getBody();
    }
}
