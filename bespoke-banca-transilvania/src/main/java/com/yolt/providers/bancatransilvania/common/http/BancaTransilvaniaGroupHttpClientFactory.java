package com.yolt.providers.bancatransilvania.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bancatransilvania.common.auth.BancaTransilvaniaGroupAuthenticationMeans;
import com.yolt.providers.bancatransilvania.common.config.BancaTransilvaniaGroupProperties;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class BancaTransilvaniaGroupHttpClientFactory {

    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final BancaTransilvaniaGroupProperties properties;
    private final BancaTransilvaniaGroupHttpHeadersProducer headersProducer;

    public BancaTransilvaniaGroupHttpClient createHttpClient(BancaTransilvaniaGroupAuthenticationMeans authMeans,
                                                             RestTemplateManager restTemplateManager,
                                                             String providerDisplayName) {
        RestTemplateManagerConfiguration configuration = createRestTemplateManagerConfiguration(authMeans);
        RestTemplate restTemplate = restTemplateManager.manage(configuration);
        return new BancaTransilvaniaGroupHttpClient(meterRegistry, restTemplate, providerDisplayName, headersProducer);
    }

    private RestTemplateManagerConfiguration createRestTemplateManagerConfiguration(BancaTransilvaniaGroupAuthenticationMeans authMeans) {
        return new RestTemplateManagerConfiguration(authMeans.getTransportKeyId(), authMeans.getTransportCertificate(), externalRestTemplateBuilderFactory ->
                externalRestTemplateBuilderFactory
                        .externalTracing(BancaTransilvaniaGroupHttpHeadersProducer.X_REQUEST_ID_HEADER)
                        .rootUri(properties.getBaseUrl())
                        .messageConverters(getHttpMessageConverters())
                        .build());
    }

    private HttpMessageConverter<?>[] getHttpMessageConverters() {
        return new HttpMessageConverter<?>[] {
                new ProjectingJackson2HttpMessageConverter(),
                new MappingJackson2HttpMessageConverter(objectMapper),
                new FormHttpMessageConverter()
        };
    }
}
