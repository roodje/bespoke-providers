package com.yolt.providers.alpha.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.alpha.common.config.AlphaProperties;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@RequiredArgsConstructor
public class AlphaHttpClientFactory {

    private final MeterRegistry registry;
    private final AlphaProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpErrorHandlerV2 errorHandlerV2;

    public AlphaHttpClient createHttpClient(final RestTemplateManager restTemplateManager,
                                            final String provider) {
        RestTemplate restTemplate = restTemplateManager.manage(new RestTemplateManagerConfiguration(
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .rootUri(properties.getBaseUrl())
                        .messageConverters(Arrays.asList(
                                new FormHttpMessageConverter(),
                                new MappingJackson2HttpMessageConverter(objectMapper),
                                new ByteArrayHttpMessageConverter())
                        )
                        .build()));
        return new AlphaHttpClient(registry, restTemplate, provider, errorHandlerV2);
    }
}
