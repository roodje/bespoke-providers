package com.yolt.providers.monorepogroup.qontogroup.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.monorepogroup.qontogroup.common.QontoGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.qontogroup.common.mapper.QontoGroupDateMapper;
import com.yolt.providers.monorepogroup.qontogroup.qonto.QontoProperties;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import static com.yolt.providers.monorepogroup.qontogroup.qonto.QontoBeanConfig.PROVIDER_IDENTIFIER;

@RequiredArgsConstructor
public class DefaultQontoGroupHttpClientProducer implements QontoGroupHttpClientProducer {

    private final QontoProperties properties;
    private final MeterRegistry registry;
    private final ObjectMapper objectMapper;
    private final HttpErrorHandlerV2 errorHandler;
    private final QontoGroupDateMapper dateMapper;

    @Override
    public QontoGroupHttpClient createHttpClient(QontoGroupAuthenticationMeans authenticationMeans, RestTemplateManager restTemplateManager) {
        var restTemplate = getRestTemplate(restTemplateManager, objectMapper);
        return new DefaultQontoGroupHttpClient(registry, restTemplate, PROVIDER_IDENTIFIER, errorHandler, properties.getTokenUrl(), dateMapper);
    }

    private RestTemplate getRestTemplate(final RestTemplateManager restTemplateManager, final ObjectMapper objectMapper) {
        return restTemplateManager.manage(new RestTemplateManagerConfiguration(
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .externalTracing("X-Request-ID")
                        .messageConverters(
                                new ProjectingJackson2HttpMessageConverter(objectMapper),
                                new FormHttpMessageConverter(),
                                new MappingJackson2HttpMessageConverter(objectMapper))
                        .rootUri(properties.getBaseUrl())
                        .build()));
    }
}
