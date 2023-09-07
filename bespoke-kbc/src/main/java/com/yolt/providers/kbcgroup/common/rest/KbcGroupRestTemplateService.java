package com.yolt.providers.kbcgroup.common.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.kbcgroup.common.KbcGroupAuthMeans;
import com.yolt.providers.kbcgroup.common.KbcGroupProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;

@RequiredArgsConstructor
public class KbcGroupRestTemplateService {

    private final ObjectMapper objectMapper;
    private final KbcGroupProperties properties;
    private final Clock clock;

    private static final String EXTERNAL_TRACE_ID_HEADER_NAME = "X-Request-ID";

    public KbcGroupHttpClient createHttpClient(final KbcGroupAuthMeans authenticationMeans,
                                               final RestTemplateManager restTemplateManager) {
        RestTemplate restTemplate = restTemplateManager.manage( new RestTemplateManagerConfiguration(
                authenticationMeans.getTransportKeyId(),
                authenticationMeans.getClientTransportCertificate(),
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .messageConverters(new MappingJackson2HttpMessageConverter(objectMapper), new FormHttpMessageConverter())
                        .externalTracing(EXTERNAL_TRACE_ID_HEADER_NAME)
                        .rootUri(properties.getBaseUrl())
                        .build()));

        return new KbcGroupHttpClient(restTemplate, properties, clock);
    }
}