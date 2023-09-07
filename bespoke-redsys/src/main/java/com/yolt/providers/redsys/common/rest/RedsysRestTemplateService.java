package com.yolt.providers.redsys.common.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.redsys.common.auth.RedsysAuthenticationMeans;
import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class RedsysRestTemplateService {

    protected final ObjectMapper objectMapper;
    protected final RedsysBaseProperties properties;

    public RedsysHttpClient createHttpClient(final RedsysAuthenticationMeans authenticationMeans,
                                             final RestTemplateManager restTemplateManager) {
        RestTemplate restTemplate = restTemplateManager.manage(new RestTemplateManagerConfiguration(
                authenticationMeans.getTransportKeyId(),
                authenticationMeans.getTransportCertificate(),
                externalRestTemplateBuilderFactory ->
                        externalRestTemplateBuilderFactory
                                .rootUri(properties.getBaseUrl())
                                .messageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                                .build())
        );
        return new RedsysHttpClient(restTemplate);
    }

}
