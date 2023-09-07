package com.yolt.providers.direkt1822group.common.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.direkt1822group.common.Direkt1822GroupAuthenticationMeans;
import com.yolt.providers.direkt1822group.common.config.Direkt1822GroupProperties;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;

@RequiredArgsConstructor
public class Direkt1822RestTemplateService {

    private final MeterRegistry registry;
    private final ObjectMapper objectMapper;
    private final Direkt1822GroupProperties properties;

    private static final String EXTERNAL_TRACE_ID_HEADER_NAME = "X-Request-ID";

    public Direkt1822GroupHttpClient createHttpClient(Direkt1822GroupAuthenticationMeans authenticationMeans,
                                                      RestTemplateManager restTemplateManager,
                                                      String provider,
                                                      Clock clock) {
        RestTemplate restTemplate = restTemplateManager.manage(
                new RestTemplateManagerConfiguration(
                        authenticationMeans.getTransportKeyId(),
                        authenticationMeans.getClientTransportCertificate(),
                        externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                                .messageConverters(
                                        new ProjectingJackson2HttpMessageConverter(objectMapper),
                                        new MappingJackson2HttpMessageConverter(objectMapper))
                                .externalTracing(EXTERNAL_TRACE_ID_HEADER_NAME)
                                .rootUri(properties.getBaseUrl())
                                .build()));

        return new Direkt1822GroupHttpClient(registry, restTemplate, provider, new Direkt1822HttpClientErrorHandler(), clock);
    }
}
