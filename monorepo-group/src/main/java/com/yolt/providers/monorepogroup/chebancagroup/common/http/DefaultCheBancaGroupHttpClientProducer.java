package com.yolt.providers.monorepogroup.chebancagroup.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfigurationBuilder;
import com.yolt.providers.common.rest.http.DefaultHttpErrorHandlerV3;
import com.yolt.providers.monorepogroup.chebancagroup.common.CheBancaGroupProperties;
import com.yolt.providers.monorepogroup.chebancagroup.common.auth.CheBancaGroupAuthenticationMeans;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.security.cert.X509Certificate;
import java.time.Clock;
import java.util.UUID;

@RequiredArgsConstructor
public class DefaultCheBancaGroupHttpClientProducer implements CheBancaGroupHttpClientProducer {

    private final CheBancaGroupProperties properties;
    private final MeterRegistry meterRegistry;
    private final String providerIdentifier;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public CheBancaGroupHttpClient createHttpClient(final CheBancaGroupAuthenticationMeans authenticationMeans, final RestTemplateManager restTemplateManager) {
        RestTemplate restTemplate = getRestTemplate(authenticationMeans.getTransportCertificateId(), authenticationMeans.getTransportCertificate(), restTemplateManager, objectMapper);
        return new DefaultCheBancaGroupHttpClient(
                meterRegistry,
                restTemplate,
                providerIdentifier,
                new DefaultCheBancaGroupHttpHeadersProducer(clock, properties),
                properties,
                new DefaultHttpErrorHandlerV3(),
                objectMapper);
    }

    private RestTemplate getRestTemplate(final UUID transportKeyId, final X509Certificate tlsCertificate, final RestTemplateManager restTemplateManager, final ObjectMapper objectMapper) {
        return restTemplateManager.manage(RestTemplateManagerConfigurationBuilder.mutualTlsBuilder(
                        transportKeyId,
                        tlsCertificate,
                        externalRestTemplateBuilderFactory ->
                                externalRestTemplateBuilderFactory
                                        .rootUri(properties.getBaseUrl())
                                        .messageConverters(getHttpMessageConverters())
                                        .uriTemplateHandler(configureDefaultUriBuilderFactory())
                                        .build())
                .disableRedirectHandling()
                .build());
    }

    protected HttpMessageConverter[] getHttpMessageConverters() {
        return new HttpMessageConverter[]{
                new ProjectingJackson2HttpMessageConverter(objectMapper),
                new FormHttpMessageConverter(),
                new MappingJackson2HttpMessageConverter(objectMapper)
        };
    }

    protected DefaultUriBuilderFactory configureDefaultUriBuilderFactory() {
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        uriBuilderFactory.setParsePath(false);
        return uriBuilderFactory;
    }
}
