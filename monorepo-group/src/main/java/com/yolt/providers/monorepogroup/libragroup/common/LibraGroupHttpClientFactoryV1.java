package com.yolt.providers.monorepogroup.libragroup.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.common.rest.logging.LoggingInterceptor;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.LibraGroupAuthenticationHttpClient;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.LibraGroupAuthenticationHttpClientV1;
import com.yolt.providers.monorepogroup.libragroup.common.ais.data.LibraGroupDataHttpClient;
import com.yolt.providers.monorepogroup.libragroup.common.ais.data.LibraGroupDataHttpClientV1;
import com.yolt.providers.monorepogroup.libragroup.common.config.LibraGroupProperties;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class LibraGroupHttpClientFactoryV1 implements LibraGroupHttpClientFactory {

    private final MeterRegistry registry;
    private final String provider;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final LibraGroupProperties properties;
    private final HttpErrorHandlerV2 errorHandler;
    private final LibraSigningService signingService;

    @Override
    public LibraGroupAuthenticationHttpClient buildAuthorizationHttpClient(RestTemplateManager restTemplateManager) {
        return new LibraGroupAuthenticationHttpClientV1(signingService,
                errorHandler,
                registry,
                createRestTemplate(restTemplateManager),
                clock,
                provider);
    }

    @Override
    public LibraGroupDataHttpClient buildDataHttpClient(RestTemplateManager restTemplateManager) {
        return new LibraGroupDataHttpClientV1(errorHandler,
                signingService,
                registry,
                createRestTemplate(restTemplateManager),
                provider);
    }

    private RestTemplate createRestTemplate(RestTemplateManager restTemplateManager) {
        return restTemplateManager.manage(new RestTemplateManagerConfiguration(
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .messageConverters(
                                new ProjectingJackson2HttpMessageConverter(objectMapper),
                                new FormHttpMessageConverter(),
                                new MappingJackson2HttpMessageConverter(objectMapper))
                        .rootUri(properties.getBaseUrl())
                        .build()));
    }
}