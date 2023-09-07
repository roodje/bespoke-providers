package com.yolt.providers.abancagroup.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.abancagroup.common.ais.config.AbancaGroupProperties;
import com.yolt.providers.abancagroup.common.ais.data.service.AbancaSigningService;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.util.UUID;

@AllArgsConstructor
public class AbancaHttpClientFactory {

    private final MeterRegistry registry;
    private final String provider;
    private final ObjectMapper objectMapper;
    private final AbancaGroupProperties properties;
    private final HttpErrorHandlerV2 errorHandler;
    private final AbancaSigningService signingUtil;

    public AbancaHttpClient buildHttpClient(UUID transportKeyId,
                                            X509Certificate certificate,
                                            RestTemplateManager restTemplateManager) {
        RestTemplate restTemplate = restTemplateManager.manage(transportKeyId,
                new X509Certificate[]{certificate},
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .messageConverters(
                                new ProjectingJackson2HttpMessageConverter(),
                                new FormHttpMessageConverter(),
                                new MappingJackson2HttpMessageConverter(objectMapper))
                        .rootUri(properties.getBaseUrl())
                        .build());
        return new AbancaHttpClient(signingUtil,
                properties,
                errorHandler,
                registry,
                restTemplate,
                provider);
    }
}