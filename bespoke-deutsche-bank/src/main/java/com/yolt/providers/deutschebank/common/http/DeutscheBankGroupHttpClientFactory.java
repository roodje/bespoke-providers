package com.yolt.providers.deutschebank.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.deutschebank.common.auth.DeutscheBankGroupAuthenticationMeans;
import com.yolt.providers.deutschebank.common.config.DeutscheBankGroupProperties;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Deprecated
@RequiredArgsConstructor
public class DeutscheBankGroupHttpClientFactory {

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final DeutscheBankGroupProperties properties;

    public DeutscheBankGroupHttpClient createHttpClient(DeutscheBankGroupAuthenticationMeans authMeans,
                                                        RestTemplateManager restTemplateManager,
                                                        String providerDisplayName) {
        RestTemplate restTemplate = restTemplateManager.manage(new RestTemplateManagerConfiguration(
                UUID.fromString(authMeans.getTransportKeyId()),
                authMeans.getTransportCertificate(),
                externalRestTemplateBuilderFactory ->
                        externalRestTemplateBuilderFactory
                                .externalTracing(DeutscheBankGroupHttpHeadersProducer.X_REQUEST_ID_HEADER)
                                .rootUri(properties.getAisBaseUrl())
                                .messageConverters(getHttpMessageConverters())
                                .build())
        );
        return new DeutscheBankGroupHttpClientV1(
                meterRegistry,
                restTemplate,
                providerDisplayName,
                new DeutscheBankGroupHttpHeadersProducer(properties));
    }

    private HttpMessageConverter[] getHttpMessageConverters() {
        return new HttpMessageConverter[]{
                new ProjectingJackson2HttpMessageConverter(),
                new MappingJackson2HttpMessageConverter(objectMapper),
                new FormHttpMessageConverter()
        };
    }
}
