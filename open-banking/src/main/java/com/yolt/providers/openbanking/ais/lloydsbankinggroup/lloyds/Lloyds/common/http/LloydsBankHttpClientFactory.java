package com.yolt.providers.openbanking.ais.lloydsbankinggroup.lloyds.Lloyds.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfigurationBuilder;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.http.LloydsBankingGroupHttpClientFactoryV2;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.web.client.RestTemplate;

public class LloydsBankHttpClientFactory extends LloydsBankingGroupHttpClientFactoryV2 {
    private static final long KEEP_ALIVE_TIMEOUT_IN_MILLIS = 4000L;
    private final DefaultProperties properties;
    private final ObjectMapper mapper;

    public LloydsBankHttpClientFactory(DefaultProperties properties, MeterRegistry registry, ObjectMapper mapper) {
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
                        .rootUri(properties.getBaseUrl())
                        .messageConverters(getHttpMessageConverters(mapper))
                        .externalTracing(HttpExtraHeaders.INTERACTION_ID_HEADER_NAME)
                        .uriTemplateHandler(super.configureDefaultUriBuilderFactory())
                        .additionalInterceptors(getClientHttpRequestInterceptors())
                        .build())
                .defaultKeepAliveTimeoutInMillis(KEEP_ALIVE_TIMEOUT_IN_MILLIS)
                .build());
    }
}
