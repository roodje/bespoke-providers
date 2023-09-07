package com.yolt.providers.yoltprovider.pis.ukdomestic;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.yoltprovider.YoltProviderConfigurationProperties;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticConsent1;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticScheduledConsent1;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticStandingOrderConsent1;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.YoltBankUkPaymentErrorHandler;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class YoltBankUkDomesticHttpServiceImpl implements YoltBankUkDomesticHttpService {

    private final RestTemplate restTemplate;

    public YoltBankUkDomesticHttpServiceImpl(ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory,
                                             YoltBankUkPaymentErrorHandler errorHandler,
                                             YoltProviderConfigurationProperties properties) {
        this.restTemplate = externalRestTemplateBuilderFactory
                .rootUri(properties.getBaseUrl())
                .build();
        //!!!We are overriding response masking error interceptor here!!!
        restTemplate.setErrorHandler(errorHandler);
    }

    @Override
    public ResponseEntity<JsonNode> postInitiateSinglePayment(HttpEntity<OBWriteDomesticConsent1> httpEntity) {
        return restTemplate.exchange("/pis/uk/domestic/consent/single", HttpMethod.POST, httpEntity, JsonNode.class);
    }

    @Override
    public ResponseEntity<JsonNode> postInitiateScheduledPayment(HttpEntity<OBWriteDomesticScheduledConsent1> httpEntity) {
        return restTemplate.exchange("/pis/uk/domestic/consent/scheduled", HttpMethod.POST, httpEntity, JsonNode.class);
    }

    @Override
    public ResponseEntity<JsonNode> postInitiatePeriodicPayment(final HttpEntity<OBWriteDomesticStandingOrderConsent1> httpEntity) {
        return restTemplate.exchange("/pis/uk/domestic/consent/periodic", HttpMethod.POST, httpEntity, JsonNode.class);
    }

    @Override
    public ResponseEntity<JsonNode> postSubmitSinglePayment(HttpEntity<ConfirmPaymentRequest> httpEntity) {
        return restTemplate.exchange("/pis/uk/domestic/single", HttpMethod.POST, httpEntity, JsonNode.class);
    }

    @Override
    public ResponseEntity<JsonNode> postSubmitScheduledPayment(HttpEntity<ConfirmPaymentRequest> httpEntity) {
        return restTemplate.exchange("/pis/uk/domestic/scheduled", HttpMethod.POST, httpEntity, JsonNode.class);
    }

    @Override
    public ResponseEntity<JsonNode> postSubmitPeriodicPayment(HttpEntity<ConfirmPaymentRequest> httpEntity) {
        return restTemplate.exchange("/pis/uk/domestic/periodic", HttpMethod.POST, httpEntity, JsonNode.class);
    }

    @Override
    public ResponseEntity<JsonNode> getSinglePaymentStatus(HttpEntity<Void> httpEntity, String paymentId) {
        return restTemplate.exchange("/pis/uk/domestic/single/{paymentId}", HttpMethod.GET, httpEntity, JsonNode.class, paymentId);
    }

    @Override
    public ResponseEntity<JsonNode> getScheduledPaymentStatus(HttpEntity<Void> httpEntity, String paymentId) {
        return restTemplate.exchange("/pis/uk/domestic/scheduled/{paymentId}", HttpMethod.GET, httpEntity, JsonNode.class, paymentId);
    }

    @Override
    public ResponseEntity<JsonNode> getPeriodicPaymentStatus(HttpEntity<Void> httpEntity, String paymentId) {
        return restTemplate.exchange("/pis/uk/domestic/periodic/{paymentId}", HttpMethod.GET, httpEntity, JsonNode.class, paymentId);
    }
}
