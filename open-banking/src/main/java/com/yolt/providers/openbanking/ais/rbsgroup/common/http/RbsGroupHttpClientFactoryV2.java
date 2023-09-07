package com.yolt.providers.openbanking.ais.rbsgroup.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.util.UUID;

public class RbsGroupHttpClientFactoryV2 extends DefaultHttpClientFactory {

    private final DefaultProperties properties;
    private final ObjectMapper mapper;

    public RbsGroupHttpClientFactoryV2(DefaultProperties properties,
                                       MeterRegistry registry,
                                       ObjectMapper mapper) {
        super(properties, registry, mapper);
        this.properties = properties;
        this.mapper = mapper;
    }

    @Override
    protected RestTemplate getManagedRestTemplate(final RestTemplateManager restTemplateManager,
                                                  final DefaultAuthMeans authenticationMeans) {
        X509Certificate[] certificateChain = authenticationMeans.getTransportCertificatesChain();
        UUID transportKeyId = authenticationMeans.getTransportPrivateKeyId();

        return restTemplateManager.manage(transportKeyId, certificateChain, externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                .rootUri(properties.getBaseUrl())
                .messageConverters(getHttpMessageConverters(mapper))
                .externalTracing(HttpExtraHeaders.INTERACTION_ID_HEADER_NAME)
                .additionalInterceptors(getClientHttpRequestInterceptors())
                .build());

    }
}
