package com.yolt.providers.rabobank.pis.pec;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.rabobank.dto.external.SepaCreditTransfer;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class RabobankPisHttpClient extends DefaultHttpClient {

    private static final String INITIATE_PAYMENT_ENDPOINT = "/payments/payment-initiation/pis/v1/payments/sepa-credit-transfers";
    private static final String PAYMENT_STATUS_ENDPOINT = INITIATE_PAYMENT_ENDPOINT + "/{paymentId}/status";


    public RabobankPisHttpClient(MeterRegistry registry, RestTemplate restTemplate, String provider) {
        super(registry, restTemplate, provider);
    }

    public ResponseEntity<JsonNode> initiatePayment(HttpEntity<SepaCreditTransfer> httpEntity) throws TokenInvalidException {
        return exchange(INITIATE_PAYMENT_ENDPOINT, HttpMethod.POST, httpEntity, ProviderClientEndpoints.INITIATE_PAYMENT, JsonNode.class);
    }

    public ResponseEntity<JsonNode> getStatus(HttpEntity<Void> httpEntity, String paymentId) throws TokenInvalidException {
        return exchange(PAYMENT_STATUS_ENDPOINT, HttpMethod.GET, httpEntity, ProviderClientEndpoints.GET_PAYMENT_STATUS, JsonNode.class, paymentId);
    }
}
