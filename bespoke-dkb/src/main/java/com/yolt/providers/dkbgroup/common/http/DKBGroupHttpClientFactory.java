package com.yolt.providers.dkbgroup.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.logging.RawDataCensoringRule;
import com.yolt.providers.dkbgroup.common.DKBGroupProperties;
import com.yolt.providers.dkbgroup.common.auth.DKBGroupAuthMeans;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;

@RequiredArgsConstructor
public class DKBGroupHttpClientFactory {

    private final DKBGroupProperties properties;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;


    private static final String TPP_QWAC_BODY_HEADER_NAME = "TPP-QWAC-Body";

    public DKBGroupHttpClient createHttpClient(final RestTemplateManager restTemplateManager,
                                               final String provider,
                                               final DKBGroupAuthMeans authMeans) {
        var restTemplate = restTemplateManager.manage(
                authMeans.getTransportKeyId(),
                new X509Certificate[]{authMeans.getTransportCertificate()},
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .rootUri(properties.getBaseUrl())
                        .rawDataCensoringRules(
                                Collections.singletonList(new RawDataCensoringRule(TPP_QWAC_BODY_HEADER_NAME, "***"))
                        )
                        .messageConverters(Arrays.asList(
                                new FormHttpMessageConverter(),
                                new StringHttpMessageConverter(),
                                new MappingJackson2HttpMessageConverter(objectMapper),
                                new ByteArrayHttpMessageConverter())
                        )
                        .build());
        return new DKBGroupHttpClient(meterRegistry, restTemplate, provider);
    }
}
