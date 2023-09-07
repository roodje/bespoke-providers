package com.yolt.providers.openbanking.ais.revolutgroup.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfigurationBuilder;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import static java.nio.charset.StandardCharsets.UTF_8;

public class RevolutHttpClientFactoryV2 extends DefaultHttpClientFactory {

    private static final long KEEP_ALIVE_TIMEOUT_IN_MILLIS = 4000L;

    private final DefaultProperties properties;
    private final ObjectMapper mapper;

    public RevolutHttpClientFactoryV2(DefaultProperties properties, MeterRegistry registry, ObjectMapper mapper) {
        super(properties, registry, mapper);
        this.properties = properties;
        this.mapper = mapper;
    }

    @Override
    protected HttpMessageConverter[] getHttpMessageConverters(final ObjectMapper mapper) {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter(mapper);
        mappingJackson2HttpMessageConverter.setDefaultCharset(UTF_8);

        return new HttpMessageConverter[]{
                mappingJackson2HttpMessageConverter,
                new FormHttpMessageConverter(),
                new ByteArrayHttpMessageConverter(),
                new StringHttpMessageConverter()
        };
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
