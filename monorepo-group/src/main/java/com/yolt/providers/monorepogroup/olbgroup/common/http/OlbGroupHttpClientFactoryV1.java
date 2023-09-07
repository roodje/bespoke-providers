package com.yolt.providers.monorepogroup.olbgroup.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.monorepogroup.olbgroup.common.auth.OlbGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.olbgroup.common.config.OlbGroupProperties;
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
public class OlbGroupHttpClientFactoryV1 implements OlbGroupHttpClientFactory {

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final OlbGroupProperties properties;
    private final OlbGroupHttpHeadersProducer headersProducer;
    private final HttpErrorHandlerV2 errorHandler;

    @Override
    public OlbGroupHttpClient createHttpClient(OlbGroupAuthenticationMeans authMeans,
                                               RestTemplateManager restTemplateManager,
                                               String providerDisplayName) {
        RestTemplate restTemplate = restTemplateManager.manage(
                UUID.fromString(authMeans.getTransportKeyId()),
                new X509Certificate[]{authMeans.getTransportCertificate()},
                externalRestTemplateBuilderFactory ->
                        externalRestTemplateBuilderFactory
                                .externalTracing(OlbGroupHttpHeadersProducer.X_REQUEST_ID_HEADER)
                                .rootUri(properties.getBaseUrl())
                                .messageConverters(getHttpMessageConverters())
                                .build()
        );
        return new OlbGroupHttpClientV1(
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
