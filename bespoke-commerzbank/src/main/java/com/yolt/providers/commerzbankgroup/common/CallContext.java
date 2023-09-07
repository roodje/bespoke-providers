package com.yolt.providers.commerzbankgroup.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.commerzbankgroup.common.api.CommerzbankGroupApiClient;
import com.yolt.providers.commerzbankgroup.common.authmeans.CommerzbankGroupAuthenticationMeans;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfigurationBuilder;
import com.yolt.providers.common.rest.http.DefaultHttpClientV2;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class CallContext {

    private final Supplier<RestTemplateManager> restTemplateManagerSupplier;
    private final Supplier<String> psuIpAddressSupplier;
    private final Supplier<CommerzbankGroupAuthenticationMeans> commerzbankGroupAuthenticationMeans;
    private final CommerzbankBaseProperties commerzbankBaseProperties;
    private final MeterRegistry meterRegistry;
    private final String provider;
    private final ObjectMapper objectMapper;
    private static final String REQUEST_ID_HEADER_NAME = "X-Request-ID";

    private static final long KEEP_ALIVE_TIMEOUT_IN_MILLIS = 4000L;

    public CommerzbankGroupApiClient createApiClient() {
        var restTemplate = prepareRestTemplate(commerzbankGroupAuthenticationMeans.get(), restTemplateManagerSupplier.get());
        var defaultHttpClientV2 = new DefaultHttpClientV2(meterRegistry, restTemplate, provider);
        return new CommerzbankGroupApiClient(defaultHttpClientV2, commerzbankGroupAuthenticationMeans.get(), commerzbankBaseProperties.getPaginationLimit(), psuIpAddressSupplier.get());
    }

    public String serializeState(Object object, Supplier<RuntimeException> exceptionSupplier) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw exceptionSupplier.get();
        }
    }

    public <S> S deserializeState(String serializedJson, Class<S> clazz, Supplier<RuntimeException> exceptionSupplier) {
        try {
            return objectMapper.readValue(serializedJson, clazz);
        } catch (JsonProcessingException e) {
            throw exceptionSupplier.get();
        }
    }

    private RestTemplate prepareRestTemplate(CommerzbankGroupAuthenticationMeans authenticationMeans, RestTemplateManager restTemplateManager) {
        return restTemplateManager.manage(RestTemplateManagerConfigurationBuilder
                .mutualTlsBuilder(authenticationMeans.getClientCertificateKey(),
                        authenticationMeans.getClientCertificate(),
                        externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                                .rootUri(commerzbankBaseProperties.getBaseUrl())
                                .messageConverters(
                                        new MappingJackson2HttpMessageConverter(objectMapper),
                                        new FormHttpMessageConverter())
                                .externalTracing(REQUEST_ID_HEADER_NAME)
                                .build())
                .defaultKeepAliveTimeoutInMillis(KEEP_ALIVE_TIMEOUT_IN_MILLIS)
                .build());
    }
}
