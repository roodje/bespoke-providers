package com.yolt.providers.unicredit.common.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.unicredit.common.auth.UniCreditAuthMeans;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.UUID;

@RequiredArgsConstructor
public class UniCreditHttpClientFactory {

    private static final String EXTERNAL_TRACE_ID_HEADER_NAME = "X-Request-ID";

    private final MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper;
    private final UniCreditHttpHeadersProducer httpHeadersProducer;

    public UniCreditHttpClient createHttpClient(final UniCreditAuthMeans authenticationMeans,
                                                final RestTemplateManager restTemplateManager,
                                                final String providerDisplayName,
                                                final String baseUrl) {
        RestTemplate restTemplate = buildRestTemplateForClient(authenticationMeans, restTemplateManager, baseUrl);
        return new UniCreditHttpClient(meterRegistry, restTemplate, providerDisplayName, httpHeadersProducer);
    }

    private RestTemplate buildRestTemplateForClient(final UniCreditAuthMeans authenticationMeans,
                                                    final RestTemplateManager restTemplateManager,
                                                    final String baseUrl) {
        return restTemplateManager.manage(new RestTemplateManagerConfiguration(
                UUID.fromString(authenticationMeans.getEidasKeyId()),
                authenticationMeans.getEidasCertificate(),
                externalRestTemplateBuilderFactory ->
                        externalRestTemplateBuilderFactory
                                .rootUri(baseUrl)
                                .messageConverters(Arrays.asList(
                                        new ProjectingJackson2HttpMessageConverter(objectMapper),
                                        new MappingJackson2HttpMessageConverter(objectMapper)))
                                .externalTracing(EXTERNAL_TRACE_ID_HEADER_NAME)
                                .build())
        );
    }
}
