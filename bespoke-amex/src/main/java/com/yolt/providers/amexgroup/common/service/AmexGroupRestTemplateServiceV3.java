package com.yolt.providers.amexgroup.common.service;

import com.yolt.providers.amexgroup.common.AmexGroupConfigurationProperties;
import com.yolt.providers.amexgroup.common.auth.AmexGroupAuthMeans;
import com.yolt.providers.amexgroup.common.http.TokenRevocationInterceptor;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

public class AmexGroupRestTemplateServiceV3 {

    public static final String X_AMEX_REQUEST_ID_HEADER = "x-amex-request-id";
    private final AmexGroupConfigurationProperties properties;

    public AmexGroupRestTemplateServiceV3(final AmexGroupConfigurationProperties properties) {
        this.properties = properties;
    }

    RestTemplate buildRestTemplate(final AmexGroupAuthMeans authenticationMeans,
                                   final RestTemplateManager restTemplateManager) {
        return restTemplateManager.manage(new RestTemplateManagerConfiguration(authenticationMeans.getTransportKeyId(),
                authenticationMeans.getClientTransportCertificate(),
                externalRestTemplateBuilderFactory ->
                        externalRestTemplateBuilderFactory
                                .messageConverters(new MappingJackson2HttpMessageConverter(), new FormHttpMessageConverter())
                                .rootUri(properties.getBaseUrl())
                                .externalTracing(X_AMEX_REQUEST_ID_HEADER, () -> UUID.randomUUID().toString().replace("-", ""))
                                .additionalInterceptors(new TokenRevocationInterceptor())
                                .build()
        ));
    }
}