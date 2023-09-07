package com.yolt.providers.openbanking.ais.santander.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfigurationBuilder;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.web.client.RestTemplate;

public class SantanderHttpClientFactory extends DefaultHttpClientFactory {
    private static final long KEEP_ALIVE_TIMEOUT_IN_MILLIS = 2000L;
    private final DefaultProperties properties;
    private final ObjectMapper mapper;

    public SantanderHttpClientFactory(DefaultProperties properties, MeterRegistry registry, ObjectMapper mapper) {
        super(properties, registry, mapper);
        this.properties = properties;
        this.mapper = mapper;
    }

    @Override
    protected RestTemplate getManagedRestTemplate(RestTemplateManager restTemplateManager, DefaultAuthMeans authenticationMeans) {
        return restTemplateManager.manage(RestTemplateManagerConfigurationBuilder.mutualTlsBuilder(
                authenticationMeans.getTransportPrivateKeyId(),
                authenticationMeans.getTransportCertificate(),
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .messageConverters(getHttpMessageConverters(mapper))
                        .externalTracing(HttpExtraHeaders.INTERACTION_ID_HEADER_NAME)
                        .rootUri(properties.getBaseUrl())
                        .additionalInterceptors(getClientHttpRequestInterceptors())
                        .build())
                .defaultKeepAliveTimeoutInMillis(KEEP_ALIVE_TIMEOUT_IN_MILLIS)
                .build());

    }
}
