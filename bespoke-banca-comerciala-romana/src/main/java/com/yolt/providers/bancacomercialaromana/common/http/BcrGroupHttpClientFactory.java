package com.yolt.providers.bancacomercialaromana.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bancacomercialaromana.common.auth.BcrGroupAuthenticationMeans;
import com.yolt.providers.bancacomercialaromana.common.configuration.BcrGroupProperties;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.domain.logging.RawDataCensoringRule;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;

@RequiredArgsConstructor
public class BcrGroupHttpClientFactory {

    private static final String TPP_QWAC_BODY_HEADER_NAME = "TPP-QWAC-Body";

    private final BcrGroupProperties properties;
    private final MeterRegistry registry;
    private final ObjectMapper objectMapper;
    private final BcrGroupHeadersFactory headersFactory;

    public BcrGroupHttpClient getHttpClient(BcrGroupAuthenticationMeans authenticationMeans,
                                            RestTemplateManager restTemplateManager,
                                            String provider) {

        RestTemplate restTemplate = restTemplateManager.manage(new RestTemplateManagerConfiguration(
                authenticationMeans.getTransportKeyId(),
                authenticationMeans.getClientTransportCertificate(),
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .rootUri(properties.getBaseUrl())
                        .rawDataCensoringRules(
                                Collections.singletonList(new RawDataCensoringRule(TPP_QWAC_BODY_HEADER_NAME, "***"))
                        )
                        .messageConverters(Arrays.asList(
                                new FormHttpMessageConverter(),
                                new ProjectingJackson2HttpMessageConverter(),
                                new MappingJackson2HttpMessageConverter(objectMapper),
                                new ByteArrayHttpMessageConverter()))
                        .build())
        );

        return new BcrGroupHttpClient(registry, restTemplate, provider, headersFactory, properties);
    }
}
