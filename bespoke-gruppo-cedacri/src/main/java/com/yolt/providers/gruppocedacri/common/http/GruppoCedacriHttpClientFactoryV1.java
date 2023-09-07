package com.yolt.providers.gruppocedacri.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfigurationBuilder;
import com.yolt.providers.gruppocedacri.common.GruppoCedacriAuthenticationMeans;
import com.yolt.providers.gruppocedacri.common.config.GruppoCedacriProperties;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class GruppoCedacriHttpClientFactoryV1 implements GruppoCedacriHttpClientFactory {

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final GruppoCedacriProperties properties;

    private static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";

    @Override
    public GruppoCedacriHttpClient createHttpClient(GruppoCedacriAuthenticationMeans authenticationMeans,
                                                    RestTemplateManager restTemplateManager,
                                                    String provider) {
        var restTemplate = prepareRestTemplate(authenticationMeans, restTemplateManager);
        return new GruppoCedacriHttpClientV1(meterRegistry, restTemplate, provider, properties);
    }

    private RestTemplate prepareRestTemplate(GruppoCedacriAuthenticationMeans authenticationMeans, RestTemplateManager restTemplateManager) {
        return restTemplateManager.manage(RestTemplateManagerConfigurationBuilder
                .mutualTlsBuilder(authenticationMeans.getTransportKeyId(),
                        authenticationMeans.getTransportCertificate(),
                        externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                                .rootUri(properties.getBaseUrl())
                                .messageConverters(getHttpMessageConverters())
                                .externalTracing(REQUEST_ID_HEADER_NAME)
                                .build())
                .build());
    }

    private HttpMessageConverter[] getHttpMessageConverters() {
        return new HttpMessageConverter[]{
                new ProjectingJackson2HttpMessageConverter(),
                new MappingJackson2HttpMessageConverter(objectMapper),
                new FormHttpMessageConverter()
        };
    }
}
