package com.yolt.providers.deutschebank.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.deutschebank.common.auth.DeutscheBankGroupAuthenticationMeans;
import com.yolt.providers.deutschebank.common.config.DeutscheBankGroupProperties;
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
public class DeutscheBankGroupHttpClientFactoryV2 {

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final DeutscheBankGroupProperties properties;
    private final DeutscheBankGroupHttpHeadersProducer headersProducer;
    private final HttpErrorHandlerV2 errorHandler;

    public DeutscheBankGroupHttpClient createHttpClient(DeutscheBankGroupAuthenticationMeans authMeans,
                                                        RestTemplateManager restTemplateManager,
                                                        String providerDisplayName) {
        RestTemplate restTemplate = restTemplateManager.manage(
                UUID.fromString(authMeans.getTransportKeyId()),
                new X509Certificate[]{authMeans.getTransportCertificate()},
                externalRestTemplateBuilderFactory ->
                        externalRestTemplateBuilderFactory
                                .externalTracing(DeutscheBankGroupHttpHeadersProducer.X_REQUEST_ID_HEADER)
                                .rootUri(properties.getAisBaseUrl())
                                .messageConverters(getHttpMessageConverters())
                                .build()
        );
        return new DeutscheBankGroupHttpClientV2(
                meterRegistry,
                restTemplate,
                providerDisplayName,
                headersProducer,
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
