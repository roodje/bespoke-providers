package com.yolt.providers.openbanking.ais.generic2.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

@AllArgsConstructor
public class DefaultHttpClientFactory implements HttpClientFactory {

    private final DefaultProperties properties;
    private final MeterRegistry registry;
    private final ObjectMapper mapper;

    @Override
    public HttpClient createHttpClient(final RestTemplateManager restTemplateManager,
                                       final DefaultAuthMeans authenticationMeans,
                                       final String providerDisplayName) {
        RestTemplate restTemplate = getManagedRestTemplate(restTemplateManager, authenticationMeans);
        return new DefaultHttpClient(registry, restTemplate, providerDisplayName);
    }

    protected RestTemplate getManagedRestTemplate(final RestTemplateManager restTemplateManager,
                                                  final DefaultAuthMeans authenticationMeans) {
        X509Certificate certificate = authenticationMeans.getTransportCertificate();
        UUID transportKeyId = authenticationMeans.getTransportPrivateKeyId();

        return restTemplateManager.manage(new RestTemplateManagerConfiguration(transportKeyId, certificate, externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                .rootUri(properties.getBaseUrl())
                .messageConverters(getHttpMessageConverters(mapper))
                .externalTracing(HttpExtraHeaders.INTERACTION_ID_HEADER_NAME)
                .additionalInterceptors(getClientHttpRequestInterceptors())
                .build()));

    }

    protected HttpMessageConverter[] getHttpMessageConverters(final ObjectMapper mapper) {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter(mapper);
        // This  default is set manually to keep the backward-compatibility with the Content-type sent on our requests (application/json;charset=UTF-8)
        // You MIGHT NOT NEED this in new connections:
        mappingJackson2HttpMessageConverter.setDefaultCharset(UTF_8);
        return new HttpMessageConverter[]{
                mappingJackson2HttpMessageConverter,
                new FormHttpMessageConverter(),
                new StringHttpMessageConverter(),
                new ByteArrayHttpMessageConverter()
        };
    }

    protected ClientHttpRequestInterceptor[] getClientHttpRequestInterceptors() {
        return new ClientHttpRequestInterceptor[]{};
    }
}
