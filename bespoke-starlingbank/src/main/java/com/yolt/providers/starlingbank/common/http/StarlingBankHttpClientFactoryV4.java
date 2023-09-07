package com.yolt.providers.starlingbank.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.starlingbank.common.auth.StarlingBankAuthenticationMeans;
import com.yolt.providers.starlingbank.common.configuration.StarlingBankProperties;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.util.UUID;

@RequiredArgsConstructor
public class StarlingBankHttpClientFactoryV4 {

    private final MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper;
    private final StarlingBankProperties properties;
    private final HttpErrorHandler errorHandler;

    public StarlingBankHttpClient createHttpClient(RestTemplateManager restTemplateManager,
                                                   String providerDisplayName,
                                                   StarlingBankHttpHeadersProducer headersProducer,
                                                   StarlingBankAuthenticationMeans authMeans) {
        X509Certificate certificate = authMeans.getTransportCertificate();
        UUID transportKeyId = UUID.fromString(authMeans.getTransportKeyId());
        RestTemplate restTemplate = restTemplateManager.manage(new RestTemplateManagerConfiguration(
                transportKeyId,
                certificate,
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .rootUri(properties.getBaseUrl())
                        .messageConverters(getHttpMessageConverters())
                        .build()
        ));
        return new StarlingBankHttpClient(meterRegistry, restTemplate, providerDisplayName, headersProducer, errorHandler);
    }

    private HttpMessageConverter[] getHttpMessageConverters() {
        return new HttpMessageConverter[]{
                new MappingJackson2HttpMessageConverter(objectMapper),
                new FormHttpMessageConverter()
        };
    }
}
