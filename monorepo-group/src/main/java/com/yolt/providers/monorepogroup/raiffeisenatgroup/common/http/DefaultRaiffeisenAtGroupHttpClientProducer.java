package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV2;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.RaiffeisenAtGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.config.RaiffeisenAtGroupProperties;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.DefaultRaiffeisenAtGroupDateMapper;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.ZoneId;
import java.util.UUID;

@RequiredArgsConstructor
public class DefaultRaiffeisenAtGroupHttpClientProducer implements RaiffeisenAtGroupHttpClientProducer {

    private final RaiffeisenAtGroupProperties properties;
    private final MeterRegistry meterRegistry;
    private final String providerIdentifier;

    private final ObjectMapper objectMapper;

    private final ZoneId zoneId;

    private final Clock clock;

    @Override
    public RaiffeisenAtGroupHttpClient createHttpClient(final RaiffeisenAtGroupAuthenticationMeans authenticationMeans, final RestTemplateManager restTemplateManager) {
        RestTemplate restTemplate = getRestTemplate(authenticationMeans.getTransportCertificateId(), authenticationMeans.getTransportCertificate(), restTemplateManager, objectMapper);
        return new DefaultRaiffeisenAtGroupHttpClient(meterRegistry, restTemplate, providerIdentifier, new DefaultRaiffeisenAtGroupHttpHeadersProducer(), new DefaultRaiffeisenAtGroupDateMapper(zoneId, clock), properties, new DefaultHttpErrorHandlerV2());
    }


    private RestTemplate getRestTemplate(final UUID transportKeyId, final X509Certificate tlsCertificate, final RestTemplateManager restTemplateManager, final ObjectMapper objectMapper) {
        return restTemplateManager.manage(transportKeyId,
                new X509Certificate[]{tlsCertificate},
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .externalTracing("X-Request-ID")
                        .messageConverters(
                                new ProjectingJackson2HttpMessageConverter(),
                                new MappingJackson2HttpMessageConverter(objectMapper),
                                new FormHttpMessageConverter())
                        .rootUri(properties.getBaseUrl())
                        .build());
    }
}
