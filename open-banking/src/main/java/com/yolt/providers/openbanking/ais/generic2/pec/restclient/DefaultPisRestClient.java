package com.yolt.providers.openbanking.ais.generic2.pec.restclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class DefaultPisRestClient implements PisRestClient {

    private final HttpErrorHandler httpErrorHandler;

    @Override
    public ResponseEntity<JsonNode> createPayment(HttpClient httpClient,
                                                  String exchangePath,
                                                  HttpEntity<?> httpEntity) throws TokenInvalidException {
        return httpClient.exchange(exchangePath,
                HttpMethod.POST,
                httpEntity,
                ProviderClientEndpoints.INITIATE_PAYMENT,
                JsonNode.class,
                httpErrorHandler);
    }

    @Override
    public ResponseEntity<JsonNode> submitPayment(HttpClient httpClient, String exchangePath, HttpEntity<?> httpEntity) throws TokenInvalidException {
        return httpClient.exchange(exchangePath,
                HttpMethod.POST,
                httpEntity,
                ProviderClientEndpoints.SUBMIT_PAYMENT,
                JsonNode.class,
                httpErrorHandler);
    }

    @Override
    public ResponseEntity<JsonNode> getPaymentStatus(HttpClient httpClient, String exchangePath, HttpEntity<?> httpEntity) throws TokenInvalidException {
        return httpClient.exchange(exchangePath,
                HttpMethod.GET,
                httpEntity,
                ProviderClientEndpoints.PAYMENT_STATUS,
                JsonNode.class,
                httpErrorHandler);
    }

    @Override
    public ResponseEntity<JsonNode> getConsentStatus(HttpClient httpClient, String exchangePath, HttpEntity<?> httpEntity) throws TokenInvalidException {
        return httpClient.exchange(exchangePath,
                HttpMethod.GET,
                httpEntity,
                ProviderClientEndpoints.PAYMENT_CONSENT_STATUS,
                JsonNode.class,
                httpErrorHandler);
    }
}
