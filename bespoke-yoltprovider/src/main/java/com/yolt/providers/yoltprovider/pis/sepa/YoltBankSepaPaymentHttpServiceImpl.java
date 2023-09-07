package com.yolt.providers.yoltprovider.pis.sepa;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.yoltprovider.YoltProviderConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class YoltBankSepaPaymentHttpServiceImpl implements YoltBankSepaPaymentHttpService {

    private final RestTemplate restTemplate;

    public YoltBankSepaPaymentHttpServiceImpl(final ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory,
                                              final YoltProviderConfigurationProperties properties) {
        List<HttpMessageConverter<?>> allMessageConverters = prepareMessageConverters(externalRestTemplateBuilderFactory);
        this.restTemplate = externalRestTemplateBuilderFactory
                .rootUri(properties.getBaseUrl())
                .messageConverters(allMessageConverters).build();
    }

    private List<HttpMessageConverter<?>> prepareMessageConverters(final ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory) {
        // List of message converters with ByteArrayHttpMessageConverter on position 1. It's used when sending the request.
        // Then the remainder contains at least a generic jackson objectmapper.
        List<HttpMessageConverter<?>> messageConverters =
                externalRestTemplateBuilderFactory.build().getMessageConverters();
        List<HttpMessageConverter<?>> allMessageConverters = new ArrayList<>();
        allMessageConverters.add(new ByteArrayHttpMessageConverter());
        allMessageConverters.addAll(messageConverters);
        return allMessageConverters;
    }

    @Override
    public ResponseEntity<JsonNode> postInitiateSinglePaymentRequest(HttpEntity<byte[]> requestEntity) {
        return restTemplate.exchange(
                "/pis/sepa/single/initiate-payment",
                HttpMethod.POST,
                requestEntity,
                JsonNode.class);
    }

    @Override
    public ResponseEntity<JsonNode> postInitiatePeriodicPaymentRequest(HttpEntity<byte[]> requestEntity) {
        return restTemplate.exchange(
                "/pis/sepa/periodic/initiate-payment",
                HttpMethod.POST,
                requestEntity,
                JsonNode.class);
    }

    @Override
    public ResponseEntity<JsonNode> postSubmitSinglePaymentRequest(HttpEntity<Void> requestEntity, String paymentId) {
        return restTemplate.exchange("/pis/sepa/single/{paymentId}/submit",
                HttpMethod.POST, requestEntity, JsonNode.class, paymentId);
    }

    @Override
    public ResponseEntity<JsonNode> postSubmitPeriodicPaymentRequest(HttpEntity<Void> requestEntity, String paymentId) {
        return restTemplate.exchange("/pis/sepa/periodic/{paymentId}/submit",
                HttpMethod.POST, requestEntity, JsonNode.class, paymentId);
    }

    @Override
    public ResponseEntity<JsonNode> getSingleStatus(HttpEntity<Void> requestEntity, String paymentId) {
        return restTemplate.exchange("/pis/sepa/single/{paymentId}/status",
                HttpMethod.GET, requestEntity, JsonNode.class, paymentId);
    }

    @Override
    public ResponseEntity<JsonNode> getPeriodicStatus(HttpEntity<Void> requestEntity, String paymentId) {
        return restTemplate.exchange("/pis/sepa/periodic/{paymentId}/status",
                HttpMethod.GET, requestEntity, JsonNode.class, paymentId);
    }
}
