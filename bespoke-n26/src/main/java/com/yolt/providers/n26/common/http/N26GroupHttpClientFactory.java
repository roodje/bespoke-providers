package com.yolt.providers.n26.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.domain.RestTemplateManagerConfigurationBuilder;
import com.yolt.providers.n26.common.auth.N26GroupAuthenticationMeans;
import com.yolt.providers.n26.common.config.BaseN26Properties;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class N26GroupHttpClientFactory {

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final BaseN26Properties properties;

    public N26GroupHttpClient createHttpClient(N26GroupAuthenticationMeans authMeans,
                                               RestTemplateManager restTemplateManager,
                                               String providerDisplayName) {
        RestTemplateManagerConfiguration restTemplateManagerConfiguration = RestTemplateManagerConfigurationBuilder
                .mutualTlsBuilder(authMeans.getTransportKeyId(),
                        authMeans.getTransportCertificate(),
                        externalRestTemplateBuilderFactory ->
                                externalRestTemplateBuilderFactory
                                        .externalTracing(N26GroupHttpHeadersProducer.X_REQUEST_ID_HEADER)
                                        .rootUri(properties.getBaseUrl())
                                        .messageConverters(getHttpMessageConverters())
                                        .build())
                .disableRedirectHandling()
                .build();
        RestTemplate restTemplate = restTemplateManager.manage(restTemplateManagerConfiguration);

        return new N26GroupHttpClient(
                meterRegistry,
                restTemplate,
                providerDisplayName,
                new N26GroupHttpHeadersProducer());
    }

    private HttpMessageConverter[] getHttpMessageConverters() {
        return new HttpMessageConverter[]{
                new ProjectingJackson2HttpMessageConverter(),
                new StringHttpMessageConverter(),
                new MappingJackson2HttpMessageConverter(objectMapper),
                new FormHttpMessageConverter()
        };
    }
}
