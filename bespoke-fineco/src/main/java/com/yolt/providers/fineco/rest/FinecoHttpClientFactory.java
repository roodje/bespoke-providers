package com.yolt.providers.fineco.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.rest.http.DefaultHttpClientV2;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.fineco.auth.FinecoAuthenticationMeans;
import com.yolt.providers.fineco.config.FinecoProperties;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@RequiredArgsConstructor
public class FinecoHttpClientFactory {

    public static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final FinecoProperties properties;


    public HttpClient createHttpClient(FinecoAuthenticationMeans authenticationMeans,
                                       RestTemplateManager restTemplateManager,
                                       String providerIdentifier) {
        RestTemplate restTemplate = restTemplateManager.manage(
                new RestTemplateManagerConfiguration(
                        UUID.fromString(authenticationMeans.getClientCertificateKey()),
                        authenticationMeans.getClientCertificate(),
                        externalRestTemplateBuilderFactory ->
                                externalRestTemplateBuilderFactory
                                        .messageConverters(
                                                new ProjectingJackson2HttpMessageConverter(objectMapper),
                                                new MappingJackson2HttpMessageConverter(objectMapper))
                                        .rootUri(properties.getBaseUrl())
                                        .externalTracing(REQUEST_ID_HEADER_NAME)
                                        .build()
                )
        );

        return new DefaultHttpClientV2(meterRegistry, restTemplate, providerIdentifier);
    }
}
