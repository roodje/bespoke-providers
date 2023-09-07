package com.yolt.providers.consorsbankgroup.common.ais.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.consorsbankgroup.common.ais.DefaultProperties;
import com.yolt.providers.consorsbankgroup.common.ais.service.DefaultAuthenticationMeans;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class HttpClientFactory {

    public static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final DefaultProperties properties;


    public HttpClient createHttpClient(final DefaultAuthenticationMeans authenticationMeans,
                                       final RestTemplateManager restTemplateManager,
                                       final String providerIdentifier) {
        RestTemplate restTemplate = restTemplateManager.manage(
                new RestTemplateManagerConfiguration(
                        authenticationMeans.getClientTransportKeyId(),
                        authenticationMeans.getClientTransportCertificate(),
                        externalRestTemplateBuilderFactory ->
                                externalRestTemplateBuilderFactory
                                        .messageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                                        .externalTracing(REQUEST_ID_HEADER_NAME)
                                        .rootUri(properties.getBaseUrl())
                                        .build()
                )
        );

        return new DefaultHttpClient(meterRegistry, restTemplate, providerIdentifier);
    }
}
