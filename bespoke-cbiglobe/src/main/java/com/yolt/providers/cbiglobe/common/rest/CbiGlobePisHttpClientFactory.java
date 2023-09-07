package com.yolt.providers.cbiglobe.common.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfigurationBuilder;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.UUID;

@RequiredArgsConstructor
public class CbiGlobePisHttpClientFactory {

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final CbiGlobeBaseProperties properties;

    public CbiGlobePisHttpClient createPisHttpClient(final CbiGlobeAuthenticationMeans authenticationMeans,
                                                     final RestTemplateManager restTemplateManager,
                                                     final String provider) {
        var restTemplate = prepareRestTemplate(authenticationMeans, restTemplateManager);
        return new CbiGlobePisHttpClient(meterRegistry, restTemplate, provider);
    }

    private RestTemplate prepareRestTemplate(CbiGlobeAuthenticationMeans authenticationMeans, RestTemplateManager restTemplateManager) {
        return restTemplateManager.manage(RestTemplateManagerConfigurationBuilder
                .mutualTlsBuilder(UUID.fromString(authenticationMeans.getTransportKeyId()),
                        authenticationMeans.getTransportCertificate(),
                        externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                                .rootUri(properties.getBasePaymentsUrl())
                                .messageConverters(Arrays.asList(
                                        new ProjectingJackson2HttpMessageConverter(objectMapper),
                                        new MappingJackson2HttpMessageConverter(objectMapper),
                                        new FormHttpMessageConverter(),
                                        new ByteArrayHttpMessageConverter()))
                                .build())
                .build());
    }
}
