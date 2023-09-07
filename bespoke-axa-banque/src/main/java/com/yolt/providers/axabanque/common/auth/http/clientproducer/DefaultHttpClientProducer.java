package com.yolt.providers.axabanque.common.auth.http.clientproducer;

import com.yolt.providers.axabanque.common.auth.http.client.AuthorizationHttpClient;
import com.yolt.providers.axabanque.common.auth.http.client.DefaultAuthorizationHttpClientV2;
import com.yolt.providers.axabanque.common.auth.http.headerproducer.AuthorizationRequestHeadersProducer;
import com.yolt.providers.axabanque.common.config.GroupProperties;
import com.yolt.providers.axabanque.common.fetchdata.http.client.DefaultFetchDataHttpClientV2;
import com.yolt.providers.axabanque.common.fetchdata.http.client.FetchDataHttpClient;
import com.yolt.providers.axabanque.common.fetchdata.http.headerproducer.FetchDataRequestHeadersProducer;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@AllArgsConstructor
public class DefaultHttpClientProducer implements HttpClientProducer {
    private final MeterRegistry meterRegistry;
    private final AuthorizationRequestHeadersProducer authorizationHeadersProducer;
    private final FetchDataRequestHeadersProducer fetchDataHeadersProducer;
    private final HttpErrorHandlerV2 authorizationErrorHandler;
    private final HttpErrorHandlerV2 fetchDataErrorHandler;
    private final GroupProperties properties;
    private final String endpointVersion;
    private final String providerDisplayName;

    @Override
    public AuthorizationHttpClient getAuthenticationHttpClient(UUID transportKeyId, X509Certificate tlsCertificate, RestTemplateManager restTemplateManager) {
        RestTemplate restTemplate = getRestTemplate(transportKeyId, tlsCertificate, restTemplateManager);
        return new DefaultAuthorizationHttpClientV2(meterRegistry, restTemplate, providerDisplayName, endpointVersion, authorizationHeadersProducer, authorizationErrorHandler);
    }

    private RestTemplate getRestTemplate(UUID transportKeyId, X509Certificate tlsCertificate, RestTemplateManager restTemplateManager) {
        return restTemplateManager.manage(transportKeyId,
                new X509Certificate[]{tlsCertificate},
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .messageConverters(
                                new ProjectingJackson2HttpMessageConverter(),
                                new MappingJackson2HttpMessageConverter(),
                                new FormHttpMessageConverter())
                        .rootUri(properties.getBaseUrl())
                        .build());
    }

    @Override
    public FetchDataHttpClient getFetchDataHttpClient(UUID transportKeyId, X509Certificate tlsCertificate, RestTemplateManager restTemplateManager) {
        RestTemplate restTemplate = getRestTemplate(transportKeyId, tlsCertificate, restTemplateManager);
        return new DefaultFetchDataHttpClientV2(meterRegistry, restTemplate, providerDisplayName, endpointVersion, fetchDataHeadersProducer, DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC), fetchDataErrorHandler);
    }
}
