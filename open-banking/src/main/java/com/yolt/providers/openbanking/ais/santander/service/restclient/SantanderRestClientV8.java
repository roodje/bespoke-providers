package com.yolt.providers.openbanking.ais.santander.service.restclient;

import com.yolt.providers.common.exception.ProviderRequestFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import com.yolt.providers.openbanking.ais.santander.http.SantanderHttpErrorHandlerV2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Collections;

public class SantanderRestClientV8 extends DefaultRestClient {

    private static final String CONSENT_NOT_AUTHORIZED_MESSAGE = "ConsentNotAuthorised";

    public SantanderRestClientV8(PaymentRequestSigner payloadSigner) {
        super(payloadSigner);
    }

    @Override
    protected HttpErrorHandler getErrorHandler() {
        return SantanderHttpErrorHandlerV2.SANTANDER_HTTP_ERROR_HANDLER;
    }

    @Override
    protected HttpHeaders getHeaders(final AccessMeans clientAccessToken, final String institutionId) {
        HttpHeaders headers = super.getHeaders(clientAccessToken, institutionId);
        headers.replace(HttpHeaders.ACCEPT, Collections.singletonList("application/json, application/*+json"));
        return headers;
    }

    @Override
    public void deleteAccountAccessConsent(final HttpClient httpClient,
                                           final String exchangePath,
                                           final AccessMeans clientAccessToken,
                                           final String consentId,
                                           final DefaultAuthMeans authMeans)
            throws TokenInvalidException {
        try {
            httpClient.exchange(exchangePath + "/" + consentId,
                    HttpMethod.DELETE,
                    new HttpEntity<>(getHeaders(clientAccessToken, authMeans.getInstitutionId())),
                    ProviderClientEndpoints.DELETE_ACCOUNT_REQUEST,
                    Void.class);
        } catch (HttpStatusCodeException e) {
            // Santander's REST API returns 400 code if the account has already been deleted.
            if (HttpStatus.BAD_REQUEST.equals(e.getStatusCode())) {
                return;
            }
            String msg = String.format("Unable to delete account-request %s. Received error code %s.",
                    consentId, e.getStatusCode().value());
            throw new ProviderRequestFailedException(msg, e);
        }
    }

    @Override
    public <T> T fetchAccounts(final HttpClient httpClient,
                               final String currentPath,
                               final AccessMeans accessToken,
                               final String institutionId,
                               final Class<T> responseType)
            throws TokenInvalidException {
        try {
            return httpClient.exchange(currentPath,
                    HttpMethod.GET,
                    new HttpEntity<>(getHeaders(accessToken, institutionId)),
                    ProviderClientEndpoints.GET_ACCOUNTS,
                    responseType)
                    .getBody();
        } catch (HttpStatusCodeException e) {
            if (isTokenInvalid(e)) {
                throw new TokenInvalidException("Received an error status code while fetching accounts. Token is no longer valid.");
            }
            throw e;
        }
    }

    private boolean isTokenInvalid(HttpStatusCodeException e) {
        HttpStatus statusCode = e.getStatusCode();
        return HttpStatus.FORBIDDEN.equals(statusCode) ||
               (HttpStatus.BAD_REQUEST.equals(statusCode) &&
                e.getResponseBodyAsString().contains(CONSENT_NOT_AUTHORIZED_MESSAGE));
    }
}