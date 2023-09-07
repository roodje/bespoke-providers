package com.yolt.providers.brdgroup.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.brdgroup.common.BrdGroupAuthenticationMeans;
import com.yolt.providers.brdgroup.common.config.BrdGroupProperties;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;

@RequiredArgsConstructor
public class BrdGroupHttpClientFactoryV1 implements BrdGroupHttpClientFactory {

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final BrdGroupProperties properties;

    @Override
    public BrdGroupHttpClient createHttpClient(BrdGroupAuthenticationMeans authMeans,
                                               RestTemplateManager restTemplateManager,
                                               String providerDisplayName) {
        RestTemplate restTemplate = restTemplateManager.manage(
                authMeans.getTransportKeyId(),
                new X509Certificate[]{authMeans.getTransportCertificate()},
                externalRestTemplateBuilderFactory ->
                        externalRestTemplateBuilderFactory
                                .externalTracing("x-request-id")
                                .rootUri(properties.getBaseUrl())
                                .messageConverters(getHttpMessageConverters())
                                .build()
        );
        return new BrdGroupHttpClientV1(
                meterRegistry,
                restTemplate,
                providerDisplayName);
    }

    private HttpMessageConverter[] getHttpMessageConverters() {
        return new HttpMessageConverter[]{
                new ProjectingJackson2HttpMessageConverter(),
                new MappingJackson2HttpMessageConverter(objectMapper),
                new FormHttpMessageConverter()
        };
    }
}
