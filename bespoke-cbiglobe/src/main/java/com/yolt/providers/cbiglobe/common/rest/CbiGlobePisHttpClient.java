package com.yolt.providers.cbiglobe.common.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.cbiglobe.common.model.InitiatePaymentRequest;
import com.yolt.providers.cbiglobe.common.model.TokenResponse;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class CbiGlobePisHttpClient extends DefaultHttpClient {

    private static final String INITIATE_PAYMENT_URL = "/2.3.2/payments/sepa-credit-transfers";
    private static final String GET_PAYMENT_STATUS_URL = "/2.3.2/payments/sepa-credit-transfers/{paymentId}/status";

    public CbiGlobePisHttpClient(MeterRegistry registry, RestTemplate restTemplate, String provider) {
        super(registry, restTemplate, provider);
    }

    public TokenResponse getAccessToken(final String url, final HttpHeaders clientCredentialsHeaders) throws TokenInvalidException {
        return exchange(url,
                HttpMethod.POST,
                new HttpEntity<>(clientCredentialsHeaders),
                ProviderClientEndpoints.CLIENT_CREDENTIALS_GRANT,
                TokenResponse.class).getBody();
    }

    public ResponseEntity<JsonNode> initiatePayment(final HttpEntity<InitiatePaymentRequest> httpEntity) throws TokenInvalidException {
        return exchange(INITIATE_PAYMENT_URL,
                HttpMethod.POST,
                httpEntity,
                ProviderClientEndpoints.INITIATE_PAYMENT,
                JsonNode.class);
    }

    public ResponseEntity<JsonNode> getPaymentStatus(final HttpEntity<Void> httpEntity, final String paymentId) throws TokenInvalidException {
        return exchange(GET_PAYMENT_STATUS_URL,
                HttpMethod.GET,
                httpEntity,
                ProviderClientEndpoints.GET_PAYMENT_STATUS,
                JsonNode.class,
                paymentId);
    }
}