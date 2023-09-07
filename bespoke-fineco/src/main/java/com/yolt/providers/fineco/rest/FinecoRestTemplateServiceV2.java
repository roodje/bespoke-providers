package com.yolt.providers.fineco.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.fineco.auth.FinecoAuthenticationMeans;
import com.yolt.providers.fineco.config.FinecoProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class FinecoRestTemplateServiceV2 {

    private final ObjectMapper objectMapper;
    private final FinecoProperties properties;

    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    public FinecoRestTemplateServiceV2(@Qualifier("FinecoObjectMapper") final ObjectMapper objectMapper,
                                       final FinecoProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public FinecoHttpClientV2 createHttpClient(final FinecoAuthenticationMeans authenticationMeans,
                                               final RestTemplateManager restTemplateManager) {
        RestTemplate restTemplate = createRestTemplate(authenticationMeans, restTemplateManager);
        return new FinecoHttpClientV2(restTemplate);
    }

    private RestTemplate createRestTemplate(final FinecoAuthenticationMeans authenticationMeans, final RestTemplateManager restTemplateManager) {
        return restTemplateManager.manage(new RestTemplateManagerConfiguration(
                UUID.fromString(authenticationMeans.getClientCertificateKey()),
                authenticationMeans.getClientCertificate(),
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .rootUri(properties.getBaseUrl())
                        .messageConverters(
                                new ProjectingJackson2HttpMessageConverter(objectMapper),
                                new MappingJackson2HttpMessageConverter(objectMapper))
                        .externalTracing(REQUEST_ID_HEADER)
                        .build())
        );
    }
}