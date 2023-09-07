package com.yolt.providers.fabric.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfigurationBuilder;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.fabric.common.auth.FabricGroupAuthenticationMeans;
import com.yolt.providers.fabric.common.beanconfig.FabricGroupProperties;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.util.UUID;

@AllArgsConstructor
public class FabricGroupHttpClientFactory {

    private final ObjectMapper objectMapper;
    private final FabricGroupProperties properties;
    private final MeterRegistry meterRegistry;
    private final DefaultAuthorizationRequestHeadersProducer headersProducer;
    private final String endpointVersion;
    private final String providerDisplayName;
    private final HttpErrorHandlerV2 fetchDataErrorHandler;

    public static RestTemplate createRestTemplateWithManagedMutualTLSTemplateForOnboarding(final RestTemplateManager manager,
                                                                                           final FabricGroupAuthenticationMeans clientConfiguration,
                                                                                           final String baseUrl) {
        UUID transportKeyId = clientConfiguration.getTransportKeyId();
        X509Certificate transportCertificates = clientConfiguration.getClientTransportCertificate();

        return manager.manage(RestTemplateManagerConfigurationBuilder
                .mutualTlsBuilder(transportKeyId, transportCertificates, externalRestTemplateBuilderFactory ->
                        externalRestTemplateBuilderFactory
                        .rootUri(baseUrl)
                        .messageConverters(new MappingJackson2HttpMessageConverter(), new FormHttpMessageConverter())
                        .build())
                .build());
    }

    private RestTemplate createRestTemplate(final UUID transportKeyId, final X509Certificate tlsCertificate, final RestTemplateManager restTemplateManager) {

        return restTemplateManager.manage(RestTemplateManagerConfigurationBuilder
                .mutualTlsBuilder(transportKeyId,
                        tlsCertificate,
                        externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                                .rootUri(properties.getBaseUrl())
                                .messageConverters(getHttpMessageConverters())
                                .build())
                .build());
    }

    public FabricDefaultHttpClient getMutualTlsClient(final UUID transportKeyId,
                                                      final X509Certificate tlsCertificate,
                                                      final RestTemplateManager restTemplateManager) {
        RestTemplate restTemplateWithManagedMutualTLSTemplate = createRestTemplate(transportKeyId, tlsCertificate, restTemplateManager);
        return new FabricDefaultHttpClient(
                meterRegistry,
                endpointVersion,
                headersProducer,
                restTemplateWithManagedMutualTLSTemplate,
                providerDisplayName,
                fetchDataErrorHandler);
    }

    private HttpMessageConverter[] getHttpMessageConverters() {
        return new HttpMessageConverter[]{
                new ProjectingJackson2HttpMessageConverter(),
                new MappingJackson2HttpMessageConverter(objectMapper),
                new FormHttpMessageConverter()
        };
    }
}
