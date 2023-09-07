package com.yolt.providers.raiffeisenbank.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.raiffeisenbank.common.ais.config.RaiffeisenBankProperties;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.time.Clock;
import java.util.UUID;

@AllArgsConstructor
public class RaiffeisenBankHttpClientFactory {

    private static final String EXTERNAL_TRACE_ID_HEADER_NAME = "X-Request-ID";

    private final MeterRegistry registry;
    private final String provider;
    private final ObjectMapper objectMapper;
    private final RaiffeisenBankProperties properties;
    private final HttpErrorHandlerV2 errorHandler;
    private final Clock clock;

    public RaiffeisenBankHttpClient buildHttpClient(UUID transportKeyId,
                                                    X509Certificate certificate,
                                                    RestTemplateManager restTemplateManager) {
        RestTemplate restTemplate = restTemplateManager.manage(transportKeyId,
                new X509Certificate[]{certificate},
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .messageConverters(
                                new ProjectingJackson2HttpMessageConverter(),
                                new FormHttpMessageConverter(),
                                new MappingJackson2HttpMessageConverter(objectMapper))
                        .externalTracing(EXTERNAL_TRACE_ID_HEADER_NAME)
                        .rootUri(properties.getBaseUrl())
                        .build());
        return new RaiffeisenBankHttpClient(properties,
                errorHandler,
                registry,
                restTemplate,
                clock,
                provider);
    }
}