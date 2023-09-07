package com.yolt.providers.openbanking.ais.monzogroup.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.security.cert.X509Certificate;
import java.util.UUID;

public class MonzoGroupHttpClientFactoryV2 extends DefaultHttpClientFactory {

    private final DefaultProperties properties;
    private final ObjectMapper mapper;

    public MonzoGroupHttpClientFactoryV2(DefaultProperties properties,
                                         MeterRegistry registry,
                                         ObjectMapper mapper) {
        super(properties, registry, mapper);
        this.properties = properties;
        this.mapper = mapper;
    }

    @Override
    protected RestTemplate getManagedRestTemplate(final RestTemplateManager restTemplateManager,
                                                  final DefaultAuthMeans authenticationMeans) {
        X509Certificate certificate = authenticationMeans.getTransportCertificate();
        UUID transportKeyId = authenticationMeans.getTransportPrivateKeyId();

        return restTemplateManager.manage(new RestTemplateManagerConfiguration(transportKeyId, certificate, externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                .rootUri(properties.getBaseUrl())
                .messageConverters(getHttpMessageConverters(mapper))
                .externalTracing(HttpExtraHeaders.INTERACTION_ID_HEADER_NAME)
                .additionalInterceptors(getClientHttpRequestInterceptors())
                .uriTemplateHandler(configureDefaultUriBuilderFactory())
                .build()));

    }

    private DefaultUriBuilderFactory configureDefaultUriBuilderFactory() {
        DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory();
        defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        defaultUriBuilderFactory.setParsePath(false);
        return defaultUriBuilderFactory;
    }
}
