package com.yolt.providers.monorepogroup.atruviagroup.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfigurationBuilder;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class AtruviaGroupHttpClientFactoryV1 implements AtruviaGroupHttpClientFactory {

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final AtruviaGroupHttpHeadersProducer headersProducer;
    private final HttpErrorHandlerV2 errorHandler;

    @Override
    public AtruviaGroupHttpClient createHttpClient(RestTemplateManager restTemplateManager,
                                                   String providerDisplayName,
                                                   String baseUrl) {


        RestTemplate restTemplate = restTemplateManager.manage(
                RestTemplateManagerConfigurationBuilder.nonMutualTlsBuilder(externalRestTemplateBuilderFactory ->
                                externalRestTemplateBuilderFactory
                                        .messageConverters(getHttpMessageConverters())
                                        .build())
                        .build()
        );
        return new AtruviaGroupHttpClientV1(
                meterRegistry,
                restTemplate,
                providerDisplayName,
                headersProducer,
                errorHandler,
                baseUrl);
    }

    private HttpMessageConverter[] getHttpMessageConverters() {
        return new HttpMessageConverter[]{
                new ProjectingJackson2HttpMessageConverter(),
                new MappingJackson2HttpMessageConverter(objectMapper),
                new FormHttpMessageConverter()
        };
    }
}
