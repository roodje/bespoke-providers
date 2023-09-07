package com.yolt.providers.monorepogroup.cecgroup.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.monorepogroup.cecgroup.common.auth.CecGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.cecgroup.common.config.CecGroupProperties;
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
public class CecGroupHttpClientFactoryV1 implements CecGroupHttpClientFactory {

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final CecGroupProperties properties;
    private final CecGroupHttpHeadersProducer headersProducer;
    private final CecGroupHttpBodyProducer bodyProducer;
    private final HttpErrorHandlerV2 errorHandler;

    @Override
    public CecGroupHttpClient createHttpClient(CecGroupAuthenticationMeans authMeans,
                                               RestTemplateManager restTemplateManager,
                                               String providerDisplayName) {
        RestTemplate restTemplate = restTemplateManager.manage(
                UUID.fromString(authMeans.getTransportKeyId()),
                new X509Certificate[]{authMeans.getTransportCertificate()},
                externalRestTemplateBuilderFactory ->
                        externalRestTemplateBuilderFactory
                                .rootUri(properties.getBaseUrl())
                                .messageConverters(getHttpMessageConverters())
                                .build()
        );
        return new CecGroupHttpClientV1(
                meterRegistry,
                restTemplate,
                providerDisplayName,
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
