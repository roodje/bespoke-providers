package com.yolt.providers.monorepogroup;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class RestTemplateManagerMock implements RestTemplateManager {

    private final ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Override
    public RestTemplate manage(UUID privateTransportKid, X509Certificate[] clientCertificateChain, Function<ExternalRestTemplateBuilderFactory, RestTemplate> customizationFunction) {
        return customizationFunction.apply(externalRestTemplateBuilderFactory);
    }

    @Override
    public RestTemplate manage(RestTemplateManagerConfiguration restTemplateManagerConfiguration) {
        CloseableHttpClient httpClient = setupHttpClient(restTemplateManagerConfiguration);
        this.externalRestTemplateBuilderFactory.requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient));
        return restTemplateManagerConfiguration.getCustomizationFunction().apply(externalRestTemplateBuilderFactory);
    }

    @SneakyThrows
    private CloseableHttpClient setupHttpClient(RestTemplateManagerConfiguration restTemplateManagerConfiguration) {
        var httpClientBuilder = HttpClients.custom()
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD)
                        .build())
                .disableRedirectHandling()
                .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, new TrustAllStrategy()).build());
        if (restTemplateManagerConfiguration.isDisableRedirectHandling()) {
            httpClientBuilder.disableRedirectHandling();
        }
        return httpClientBuilder.build();
    }
}
