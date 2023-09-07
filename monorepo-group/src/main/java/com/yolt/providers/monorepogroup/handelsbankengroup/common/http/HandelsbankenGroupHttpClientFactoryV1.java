package com.yolt.providers.monorepogroup.handelsbankengroup.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.auth.HandelsbankenGroupAuthMeans;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.config.HandelsbankenGroupProperties;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.util.UUID;

@RequiredArgsConstructor
public class HandelsbankenGroupHttpClientFactoryV1 implements HandelsbankenGroupHttpClientFactory {

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final HandelsbankenGroupProperties properties;
    private final HandelsbankenGroupHttpHeadersProducer headersProducer;
    private final HandelsbankenGroupHttpBodyProducer bodyProducer;
    private final HttpErrorHandlerV2 errorHandler;

    @Override
    public HandelsbankenGroupHttpClient createHttpClient(HandelsbankenGroupAuthMeans authMeans,
                                                         RestTemplateManager restTemplateManager,
                                                         String provider) {
        RestTemplate restTemplate = restTemplateManager.manage(
                UUID.fromString(authMeans.getTransportKeyId()),
                new X509Certificate[]{authMeans.getTransportCertificate()},
                externalRestTemplateBuilderFactory ->
                        externalRestTemplateBuilderFactory
                                .rootUri(properties.getBaseUrl())
                                .messageConverters(getHttpMessageConverters())
                                .build()
        );
        return new HandelsbankenGroupHttpClientV1(
                meterRegistry,
                restTemplate,
                provider,
                headersProducer,
                bodyProducer,
                errorHandler);
    }

    private HttpMessageConverter[] getHttpMessageConverters() {
        return new HttpMessageConverter[]{
                new ProjectingJackson2HttpMessageConverter(),
                new MappingJackson2HttpMessageConverter(objectMapper),
                new FormHttpMessageConverter()
        };
    }
}
