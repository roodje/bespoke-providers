package com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
@RequiredArgsConstructor
public class RestTemplateManagerMock implements RestTemplateManager {

    private final ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    public RestTemplateManagerMock(Supplier<String> traceIdSupplier) {
        this.externalRestTemplateBuilderFactory = createExternalRestTemplateBuilderFactory(traceIdSupplier);
    }

    @SneakyThrows
    private ExternalRestTemplateBuilderFactory createExternalRestTemplateBuilderFactory(Supplier<String> traceIdSupplier) {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(new SSLConnectionSocketFactory(new SSLContextBuilder()
                        .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                        .build()))
                .build();

        ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory = new ExternalRestTemplateBuilderFactory();
        externalRestTemplateBuilderFactory.requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient));
        if (traceIdSupplier != null)
            externalRestTemplateBuilderFactory.externalTracing(HttpExtraHeaders.INTERACTION_ID_HEADER_NAME, traceIdSupplier);
        return externalRestTemplateBuilderFactory;
    }

    @Override
    public RestTemplate manage(UUID privateTransportKid, X509Certificate[] clientCertificateChain, Function<ExternalRestTemplateBuilderFactory, RestTemplate> customizationFunction) {
        return customizationFunction.apply(externalRestTemplateBuilderFactory);
    }

    @Override
    public RestTemplate manage(RestTemplateManagerConfiguration restTemplateManagerConfiguration) {
        return restTemplateManagerConfiguration.getCustomizationFunction().apply(externalRestTemplateBuilderFactory);
    }
}

