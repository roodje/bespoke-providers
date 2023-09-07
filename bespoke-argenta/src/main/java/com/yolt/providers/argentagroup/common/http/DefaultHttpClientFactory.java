package com.yolt.providers.argentagroup.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.argentagroup.common.CommonProperties;
import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import com.yolt.providers.common.rest.http.HttpClient;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class DefaultHttpClientFactory {

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final CommonProperties properties;


    public HttpClient createArgentaGroupHttpClient(final DefaultAuthenticationMeans authenticationMeans,
                                                   final RestTemplateManager restTemplateManager,
                                                   final String providerIdentifier) {
        return createHttpClient(authenticationMeans, restTemplateManager, providerIdentifier, properties.getBaseUrl());
    }

    private HttpClient createHttpClient(final DefaultAuthenticationMeans authenticationMeans,
                                        final RestTemplateManager restTemplateManager,
                                        final String providerIdentifier,
                                        final String rootUri) {
        RestTemplate restTemplate = restTemplateManager.manage(
                new RestTemplateManagerConfiguration(
                        authenticationMeans.getTransportKeyId(),
                        authenticationMeans.getTransportCertificate(),
                        externalRestTemplateBuilderFactory ->
                                externalRestTemplateBuilderFactory
                                        .messageConverters(
                                                new MappingJackson2HttpMessageConverter(objectMapper),
                                                new FormHttpMessageConverter()
                                        ).rootUri(rootUri)
                                        .build()
                )
        );

        return new DefaultHttpClient(meterRegistry, restTemplate, providerIdentifier);
    }
}
