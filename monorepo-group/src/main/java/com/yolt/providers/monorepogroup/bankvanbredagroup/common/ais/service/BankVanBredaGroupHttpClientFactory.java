package com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.service.BankVanBredaGroupAuthenticationHttpClient;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.data.service.BankVanBredaGroupDataHttpClient;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.config.BankVanBredaGroupProperties;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.time.Clock;
import java.util.UUID;

@AllArgsConstructor
public class BankVanBredaGroupHttpClientFactory {

    private static final String TRACE_ID_HEADER_LABEL = "X-Request-ID";
    private final MeterRegistry registry;
    private final String provider;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final BankVanBredaGroupProperties properties;
    private final HttpErrorHandlerV2 errorHandler;

    public BankVanBredaGroupAuthenticationHttpClient buildAuthorizationHttpClient(UUID transportKeyId,
                                                                                  X509Certificate certificate,
                                                                                  RestTemplateManager restTemplateManager) {
        return new BankVanBredaGroupAuthenticationHttpClient(errorHandler,
                registry,
                createRestTemplate(restTemplateManager, transportKeyId, certificate),
                clock,
                provider);
    }

    public BankVanBredaGroupDataHttpClient buildDataHttpClient(UUID transportKeyId,
                                                               X509Certificate certificate,
                                                               RestTemplateManager restTemplateManager) {
        return new BankVanBredaGroupDataHttpClient(errorHandler,
                registry,
                createRestTemplate(restTemplateManager, transportKeyId, certificate),
                provider);
    }

    private RestTemplate createRestTemplate(RestTemplateManager restTemplateManager, UUID transportKeyId, X509Certificate certificate) {
        return restTemplateManager.manage(transportKeyId,
                new X509Certificate[]{certificate},
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .messageConverters(
                                new ProjectingJackson2HttpMessageConverter(),
                                new FormHttpMessageConverter(),
                                new MappingJackson2HttpMessageConverter(objectMapper))
                        .externalTracing(TRACE_ID_HEADER_LABEL)
                        .rootUri(properties.getBaseUrl())
                        .build());
    }
}