package com.yolt.providers.belfius.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.belfius.common.auth.BelfiusGroupAuthMeans;
import com.yolt.providers.belfius.common.configuration.BelfiusBaseProperties;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.UUID;

@RequiredArgsConstructor
public class BelfiusGroupRestTemplateFactory {

    private static final String REQUEST_ID_HEADER_NAME = "Request-ID";

    private final ObjectMapper objectMapper;
    private final BelfiusBaseProperties properties;

    public RestTemplate createRestTemplateWithManagedMutualTLSTemplate(RestTemplateManager manager,
                                                                       BelfiusGroupAuthMeans authMeans) {
        UUID transportKeyId = authMeans.getClientCertificateKey();
        X509Certificate transportCertificate = authMeans.getClientCertificate();

        return manager.manage(new RestTemplateManagerConfiguration(
                transportKeyId,
                transportCertificate,
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .rootUri(properties.getBaseUrl())
                        .externalTracing(REQUEST_ID_HEADER_NAME)
                        .messageConverters(Arrays.asList(new ProjectingJackson2HttpMessageConverter(objectMapper),
                                new MappingJackson2HttpMessageConverter(objectMapper),
                                new FormHttpMessageConverter()))
                        .build())
        );
    }
}
