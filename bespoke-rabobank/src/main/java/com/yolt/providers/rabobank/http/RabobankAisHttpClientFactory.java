package com.yolt.providers.rabobank.http;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfigurationBuilder;
import com.yolt.providers.rabobank.RabobankAuthenticationMeans;
import com.yolt.providers.rabobank.config.RabobankProperties;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import static com.yolt.providers.rabobank.RabobankObjectMapperV3.OBJECT_MAPPER;

@RequiredArgsConstructor
public class RabobankAisHttpClientFactory {

    private final RabobankProperties properties;
    private final MeterRegistry meterRegistry;
    private final String providerDisplayName;
    private final ObjectMapper objectMapper;

    public RabobankAisHttpClient createRabobankHttpClient(RestTemplateManager restTemplateManager,
                                                          RabobankAuthenticationMeans authenticationMeans) {
        RestTemplate restTemplate = getRestTemplate(restTemplateManager, authenticationMeans, properties.getBaseUrl());

        return new RabobankAisHttpClient(meterRegistry, restTemplate, providerDisplayName, properties);
    }

    private RestTemplate getRestTemplate(final RestTemplateManager restTemplateManager, final RabobankAuthenticationMeans authenticationMeans, final String baseUrl) {
        return restTemplateManager.manage(RestTemplateManagerConfigurationBuilder.mutualTlsBuilder(authenticationMeans.getTransportKid(),
                authenticationMeans.getClientCertificate(),
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .rootUri(baseUrl)
                        .messageConverters(new MappingJackson2HttpMessageConverter(objectMapper), new FormHttpMessageConverter())
                        .build())
                .defaultKeepAliveTimeoutInMillis(10000L)
                .build()
        );
    }
}
