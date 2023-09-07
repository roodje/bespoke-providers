package com.yolt.providers.volksbank.common.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfigurationBuilder;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.volksbank.common.auth.VolksbankAuthenticationMeans;
import com.yolt.providers.volksbank.common.config.VolksbankBaseProperties;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class VolksbankHttpClientFactoryV2 {

    private static final long KEEP_ALIVE_TIMEOUT_IN_MILLIS = 4000L;

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final HttpErrorHandler volksbankHttpErrorHandler;
    private final VolksbankBaseProperties properties;

    private static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";

    public VolksbankHttpClientV4 createHttpClient(final VolksbankAuthenticationMeans authenticationMeans,
                                                  final RestTemplateManager restTemplateManager,
                                                  final String provider) {
        var restTemplate = prepareRestTemplate(authenticationMeans, restTemplateManager);
        return new VolksbankHttpClientV4(meterRegistry, restTemplate, provider, volksbankHttpErrorHandler);
    }

    public VolksbankPisHttpClientV2 createPisHttpClient(final VolksbankAuthenticationMeans authenticationMeans,
                                                        final RestTemplateManager restTemplateManager,
                                                        final String providerDisplayName) {
        var restTemplate = prepareRestTemplate(authenticationMeans, restTemplateManager);
        return new VolksbankPisHttpClientV2(meterRegistry, restTemplate, providerDisplayName, volksbankHttpErrorHandler);
    }

    private RestTemplate prepareRestTemplate(VolksbankAuthenticationMeans authenticationMeans, RestTemplateManager restTemplateManager) {
        return restTemplateManager.manage(RestTemplateManagerConfigurationBuilder
                .mutualTlsBuilder(authenticationMeans.getClientCertificateKey(),
                        authenticationMeans.getClientCertificate(),
                        externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                                .rootUri(properties.getBaseUrl())
                                .messageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                                .externalTracing(REQUEST_ID_HEADER_NAME)
                                .build())
                .defaultKeepAliveTimeoutInMillis(KEEP_ALIVE_TIMEOUT_IN_MILLIS)
                .build());
    }

}
