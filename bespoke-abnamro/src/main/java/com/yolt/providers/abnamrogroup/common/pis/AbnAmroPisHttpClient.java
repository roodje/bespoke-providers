package com.yolt.providers.abnamrogroup.common.pis;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.abnamrogroup.common.auth.AccessTokenResponseDTO;
import com.yolt.providers.abnamrogroup.abnamro.AbnAmroProperties;
import com.yolt.providers.abnamro.pis.SepaPayment;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class AbnAmroPisHttpClient extends DefaultHttpClient {

    private static final String PAYMENTS_URL = "/v1/payments";
    private static final String PUT_PAYMENT_URL = "/v1/payments/{transactionId}";
    private static final String GET_PAYMENT_STATUS_URL = PUT_PAYMENT_URL;

    private final AbnAmroProperties properties;
    private final HttpErrorHandler httpErrorHandler;

    public AbnAmroPisHttpClient(MeterRegistry registry,
                                RestTemplate restTemplate,
                                String provider,
                                AbnAmroProperties properties,
                                HttpErrorHandler httpErrorHandler) {
        super(registry, restTemplate, provider);
        this.properties = properties;
        this.httpErrorHandler = httpErrorHandler;
    }

    public ResponseEntity<JsonNode> initiatePayment(HttpEntity<SepaPayment> httpEntity) throws TokenInvalidException {
        return exchange(PAYMENTS_URL, HttpMethod.POST, httpEntity, ProviderClientEndpoints.INITIATE_PAYMENT, JsonNode.class, httpErrorHandler);
    }

    public AccessTokenResponseDTO getPisAccessToken(HttpEntity<MultiValueMap<String, String>> httpEntity) throws TokenInvalidException {
        return exchange(properties.getTokenUrl(), HttpMethod.POST, httpEntity, ProviderClientEndpoints.GET_ACCESS_TOKEN, AccessTokenResponseDTO.class, httpErrorHandler)
                .getBody();
    }

    public ResponseEntity<JsonNode> submitPayment(HttpEntity<Void> httpEntity, String transactionId) throws TokenInvalidException {
        return exchange(PUT_PAYMENT_URL, HttpMethod.PUT, httpEntity, ProviderClientEndpoints.SUBMIT_PAYMENT, JsonNode.class, httpErrorHandler, transactionId);
    }

    public ResponseEntity<JsonNode> getPaymentStatus(HttpEntity<Void> httpEntity, String transactionId) throws TokenInvalidException {
        return exchange(GET_PAYMENT_STATUS_URL, HttpMethod.GET, httpEntity, ProviderClientEndpoints.GET_PAYMENT_STATUS, JsonNode.class, httpErrorHandler, transactionId);
    }
}
