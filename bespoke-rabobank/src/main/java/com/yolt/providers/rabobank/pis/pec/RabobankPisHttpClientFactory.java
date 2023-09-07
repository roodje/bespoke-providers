package com.yolt.providers.rabobank.pis.pec;


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

@RequiredArgsConstructor
public class RabobankPisHttpClientFactory {

    private final RabobankProperties properties;
    private final MeterRegistry meterRegistry;
    private final String providerDisplayName;
    private final ObjectMapper objectMapper;

    public RabobankPisHttpClient createRabobankPisHttpClient(RestTemplateManager restTemplateManager,
                                                             RabobankAuthenticationMeans authenticationMeans) {

        RestTemplate restTemplate = restTemplateManager.manage(RestTemplateManagerConfigurationBuilder
                .mutualTlsBuilder(authenticationMeans.getTransportKid(),
                        authenticationMeans.getClientCertificate(),
                        externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                                .rootUri(properties.getBaseUrl())
                                .messageConverters(new MappingJackson2HttpMessageConverter(objectMapper), new FormHttpMessageConverter())
                                .build())
                .build());
        return new RabobankPisHttpClient(meterRegistry, restTemplate, providerDisplayName);
    }
}
