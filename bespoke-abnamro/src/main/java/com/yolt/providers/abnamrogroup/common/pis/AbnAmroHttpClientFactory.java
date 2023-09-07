package com.yolt.providers.abnamrogroup.common.pis;

import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans;
import com.yolt.providers.abnamrogroup.abnamro.AbnAmroProperties;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfigurationBuilder;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class AbnAmroHttpClientFactory {

    private final AbnAmroProperties properties;
    private final MeterRegistry meterRegistry;
    private final String providerDisplayName;
    private final HttpErrorHandler httpErrorHandler;

    public AbnAmroPisHttpClient createAbnAmroPisHttpClient(RestTemplateManager restTemplateManager,
                                                           AbnAmroAuthenticationMeans authenticationMeans) {
        RestTemplate restTemplate = restTemplateManager.manage(RestTemplateManagerConfigurationBuilder
                .mutualTlsBuilder(authenticationMeans.getClientTransportKid(),
                        authenticationMeans.getClientTransportCertificate(),
                        externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                                .rootUri(properties.getBaseUrl())
                                .additionalMessageConverters(new FormHttpMessageConverter())
                                .build())
                .build());
        return new AbnAmroPisHttpClient(meterRegistry, restTemplate, providerDisplayName, properties, httpErrorHandler);
    }
}
