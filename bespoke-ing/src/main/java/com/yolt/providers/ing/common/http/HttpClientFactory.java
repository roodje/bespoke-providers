package com.yolt.providers.ing.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.config.IngProperties;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class HttpClientFactory {

    public static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final IngProperties properties;


    public HttpClient createPisHttpClient(final IngAuthenticationMeans authenticationMeans,
                                          final RestTemplateManager restTemplateManager,
                                          final String providerIdentifier) {
        RestTemplate restTemplate = restTemplateManager.manage(
                new RestTemplateManagerConfiguration(
                        authenticationMeans.getTransportKeyId(),
                        authenticationMeans.getTlsCertificate(),
                        externalRestTemplateBuilderFactory ->
                                externalRestTemplateBuilderFactory
                                        .messageConverters(
                                                new ProjectingJackson2HttpMessageConverter(),
                                                new FormHttpMessageConverter(),
                                                new MappingJackson2HttpMessageConverter(objectMapper))
                                        .rootUri(properties.getBaseUrl())
                                        .build()
                )
        );

        return new DefaultHttpClient(meterRegistry, restTemplate, providerIdentifier);
    }
}
