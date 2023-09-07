package com.yolt.providers.openbanking.ais.nationwide.service.restclient;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.exception.ProviderRequestFailedException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.dto.AutoOnboardingResponse;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import com.yolt.providers.openbanking.ais.nationwide.NationwidePropertiesV2;
import com.yolt.providers.openbanking.ais.nationwide.http.NationwideHttpErrorHandlerV2;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Collections;

public class NationwideRestClientAisV7 extends DefaultRestClient {

    private final NationwideHttpErrorHandlerV2 errorHandler;

    public NationwideRestClientAisV7(final PaymentRequestSigner payloadSigner,
                                     final NationwidePropertiesV2 nationwideProperties) {
        super(payloadSigner);
        this.errorHandler = new NationwideHttpErrorHandlerV2(nationwideProperties);
    }

    @Override
    protected HttpErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Override
    protected HttpHeaders getPaymentHttpHeaders(final AccessMeans clientAccessToken,
                                                final DefaultAuthMeans authMeans,
                                                final Object requestBody,
                                                final Signer signer) {
        HttpHeaders headers = super.getPaymentHttpHeaders(clientAccessToken, authMeans, requestBody, signer);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    @Override
    protected HttpHeaders getHeaders(final AccessMeans clientAccessToken,
                                     final String institutionId) {
        HttpHeaders headers = super.getHeaders(clientAccessToken, institutionId);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        return headers;
    }

    @Override
    public void deleteAccountAccessConsent(final HttpClient httpClient,
                                           final String exchangePath,
                                           final AccessMeans clientAccessToken,
                                           final String consentId,
                                           final DefaultAuthMeans authMeans) throws TokenInvalidException {
        try {
            httpClient.exchange(exchangePath + "/" + consentId,
                    HttpMethod.DELETE,
                    new HttpEntity<>(getHeaders(clientAccessToken, authMeans.getInstitutionId())),
                    ProviderClientEndpoints.DELETE_ACCOUNT_ACCESS_CONSENT,
                    Void.class);
        } catch (HttpStatusCodeException e) {
            if (!HttpStatus.INTERNAL_SERVER_ERROR.equals(e.getStatusCode())) {
                String msg = String.format("Unable to delete account-access-consent %s. Received error code %s.", consentId, e.getStatusCode());
                throw new ProviderRequestFailedException(msg);
            }
        }
    }

    public AutoOnboardingResponse register(HttpClient httpClient,
                                           String payload,
                                           String registrationUrl) throws TokenInvalidException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.CONTENT_TYPE, "application/jose");
        HttpEntity<String> httpEntity = new HttpEntity<>(payload, headers);
        return httpClient.exchange(registrationUrl, HttpMethod.POST, httpEntity, ProviderClientEndpoints.REGISTER, AutoOnboardingResponse.class).getBody();
    }

    public AutoOnboardingResponse updateRegistration(HttpClient httpClient, String payload, String updateRegistrationUrl, AccessMeans accessMeans, String clientId) throws TokenInvalidException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.CONTENT_TYPE, "application/jose");
        headers.setBearerAuth(accessMeans.getAccessToken());
        HttpEntity<String> httpEntity = new HttpEntity<>(payload, headers);
        return httpClient.exchange(updateRegistrationUrl, HttpMethod.PUT, httpEntity, ProviderClientEndpoints.REGISTER, AutoOnboardingResponse.class, clientId).getBody();
    }
}
