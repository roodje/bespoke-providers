package com.yolt.providers.knabgroup.common.http;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfigurationBuilder;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.knabgroup.common.auth.KnabGroupAuthenticationMeans;
import com.yolt.providers.knabgroup.common.configuration.KnabGroupProperties;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class KnabGroupHttpClientFactory {

    private static final long KEEP_ALIVE_TIMEOUT_IN_MILLIS = 4000L;
    private static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";

    private final KnabGroupProperties properties;
    private final MeterRegistry meterRegistry;
    private final String providerDisplayName;
    private final HttpErrorHandler getLoginInfoErrorHandler;
    private final HttpErrorHandler createAccessMeansErrorHandler;
    private final HttpErrorHandler refreshAccessMeansErrorHandler;
    private final HttpErrorHandler fetchDataErrorHandler;

    public KnabGroupHttpClient createKnabGroupHttpClient(RestTemplateManager restTemplateManager,
                                                         KnabGroupAuthenticationMeans authenticationMeans) {

        RestTemplate restTemplate = restTemplateManager.manage(
                RestTemplateManagerConfigurationBuilder.mutualTlsBuilder(
                        authenticationMeans.getTransportKeyId(),
                        authenticationMeans.getTlsCertificate(),
                        externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                                .messageConverters(
                                        new ProjectingJackson2HttpMessageConverter(),
                                        new MappingJackson2HttpMessageConverter(),
                                        new FormHttpMessageConverter())
                                .rootUri(properties.getBaseUrl())
                                .externalTracing(REQUEST_ID_HEADER_NAME)
                                .build())
                        .defaultKeepAliveTimeoutInMillis(KEEP_ALIVE_TIMEOUT_IN_MILLIS)
                        .build());

        return new KnabGroupHttpClient(meterRegistry, restTemplate, providerDisplayName, properties, getLoginInfoErrorHandler, createAccessMeansErrorHandler, refreshAccessMeansErrorHandler, fetchDataErrorHandler);
    }
}
