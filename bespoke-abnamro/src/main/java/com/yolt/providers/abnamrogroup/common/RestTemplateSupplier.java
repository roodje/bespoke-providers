package com.yolt.providers.abnamrogroup.common;

import com.yolt.providers.abnamrogroup.abnamro.AbnAmroProperties;
import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class RestTemplateSupplier {

    public RestTemplate getRestTemplate(RestTemplateManager restTemplateManager,
                                        AbnAmroAuthenticationMeans authenticationMeans,
                                        AbnAmroProperties properties) {
        return restTemplateManager.manage(new RestTemplateManagerConfiguration(
                authenticationMeans.getClientTransportKid(),
                authenticationMeans.getClientTransportCertificate(),
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .rootUri(properties.getBaseUrl())
                        .additionalMessageConverters(new FormHttpMessageConverter())
                        .build()));
    }
}
