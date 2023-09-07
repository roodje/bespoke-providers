package com.yolt.providers.volksbank.common.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.volksbank.dto.v1_1.InitiatePaymentRequest;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class VolksbankPisHttpClientV2 extends DefaultHttpClient {

    private static final String INITIATE_PAYMENT_URL = "/v1/payments/sepa-credit-transfers";
    private static final String GET_PAYMENT_STATUS_URL = "/v1.1/payments/sepa-credit-transfers/{payment-id}/status";

    private final HttpErrorHandler httpErrorHandler;

    public VolksbankPisHttpClientV2(MeterRegistry registry, RestTemplate restTemplate, String provider, HttpErrorHandler httpErrorHandler) {
        super(registry, restTemplate, provider);
        this.httpErrorHandler = httpErrorHandler;
    }

    public ResponseEntity<JsonNode> initiatePayment(final HttpEntity<InitiatePaymentRequest> httpEntity) throws TokenInvalidException {
        return exchange(INITIATE_PAYMENT_URL,
                HttpMethod.POST,
                httpEntity,
                ProviderClientEndpoints.INITIATE_PAYMENT,
                JsonNode.class,
                httpErrorHandler);
    }

    public ResponseEntity<JsonNode> getPaymentStatus(final HttpEntity<Void> httpEntity, final String paymentId) throws TokenInvalidException {
        return exchange(GET_PAYMENT_STATUS_URL,
                HttpMethod.GET,
                httpEntity,
                ProviderClientEndpoints.GET_PAYMENT_STATUS,
                JsonNode.class,
                httpErrorHandler,
                paymentId);
    }
}
