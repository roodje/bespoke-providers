package com.yolt.providers.triodosbank.common.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.triodosbank.common.auth.TriodosBankAuthenticationMeans;
import com.yolt.providers.triodosbank.common.config.TriodosBankBaseProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@RequiredArgsConstructor
public class TriodosBankHttpClientFactory {

    private final TriodosBankBaseProperties properties;
    private final ObjectMapper objectMapper;

    public TriodosBankHttpClient createHttpClient(TriodosBankAuthenticationMeans authenticationMeans,
                                                  RestTemplateManager restTemplateManager,
                                                  Signer signer) {
        RestTemplate restTemplate = restTemplateManager.manage(new RestTemplateManagerConfiguration(
                UUID.fromString(authenticationMeans.getTransportKeyId()),
                authenticationMeans.getTransportCertificate(),
                externalRestTemplateBuilderFactory ->
                        externalRestTemplateBuilderFactory
                                .rootUri(properties.getBaseUrl())
                                .messageConverters(getHttpMessageConverters())
                                .build())
        );
        return new TriodosBankHttpClient(
                restTemplate,
                authenticationMeans,
                new TriodosBankHttpHeadersFactory(objectMapper),
                new TriodosBankHttpErrorHandler(),
                properties,
                signer);
    }

    private HttpMessageConverter[] getHttpMessageConverters() {
        return new HttpMessageConverter[]{
                new MappingJackson2HttpMessageConverter(objectMapper),
                new FormHttpMessageConverter(),
                new StringHttpMessageConverter()
        };
    }
}
