package com.yolt.providers.stet.generic.http.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.rest.http.DefaultHttpClientV2;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.security.cert.X509Certificate;
import java.util.UUID;

@RequiredArgsConstructor
public class DefaultHttpClientFactoryV2 implements HttpClientFactory {

    private final MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper;

    @Override
    public HttpClient createHttpClient(RestTemplateManager restTemplateManager,
                                       DefaultAuthenticationMeans authMeans,
                                       String baseUrl,
                                       String providerDisplayName) {
        RestTemplate restTemplate = createRestTemplate(restTemplateManager, authMeans, baseUrl);
        return new DefaultHttpClientV2(meterRegistry, restTemplate, providerDisplayName);
    }

    protected RestTemplate createRestTemplate(RestTemplateManager restTemplateManager,
                                              DefaultAuthenticationMeans authMeans,
                                              String baseUrl) {
        UUID transportKeyId = authMeans.getClientTransportKeyId();
        X509Certificate transportCertificate = authMeans.getClientTransportCertificate();

        return restTemplateManager.manage(
                transportKeyId,
                new X509Certificate[]{transportCertificate},
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .rootUri(baseUrl)
                        .messageConverters(getHttpMessageConverters())
                        .uriTemplateHandler(configureDefaultUriBuilderFactory())
                        .build()
        );
    }

    protected DefaultUriBuilderFactory configureDefaultUriBuilderFactory() {
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        uriBuilderFactory.setParsePath(false);
        return uriBuilderFactory;
    }

    protected HttpMessageConverter[] getHttpMessageConverters() {
        return new HttpMessageConverter[]{
                new ByteArrayHttpMessageConverter(),
                new ProjectingJackson2HttpMessageConverter(objectMapper),
                new MappingJackson2HttpMessageConverter(objectMapper),
                new FormHttpMessageConverter()
        };
    }
}
