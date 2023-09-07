package com.yolt.providers.stet.labanquepostalegroup.common.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.http.client.DefaultHttpClientFactory;
import com.yolt.providers.stet.labanquepostalegroup.labanquepostale.auth.LaBanquePostaleAuthenticationMeans;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.util.UUID;

public class LaBanquePostaleHttpClientFactory extends DefaultHttpClientFactory {
    public LaBanquePostaleHttpClientFactory(MeterRegistry meterRegistry, ObjectMapper objectMapper) {
        super(meterRegistry, objectMapper);
    }

    @Override
    protected RestTemplate createRestTemplate(RestTemplateManager restTemplateManager,
                                              DefaultAuthenticationMeans authMeans,
                                              String baseUrl)
    {
        return createRestTemplate(restTemplateManager, (LaBanquePostaleAuthenticationMeans) authMeans, baseUrl);
    }

    protected RestTemplate createRestTemplate(RestTemplateManager restTemplateManager,
                                              LaBanquePostaleAuthenticationMeans authMeans,
                                              String baseUrl) {
        UUID transportKeyId = authMeans.getClientTransportKeyId();
        X509Certificate[] transportCertificateChain = authMeans.getClientTransportCertificateChain();

        return restTemplateManager.manage(
                transportKeyId,
                transportCertificateChain,
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .rootUri(baseUrl)
                        .messageConverters(getHttpMessageConverters())
                        .uriTemplateHandler(configureDefaultUriBuilderFactory())
                        .build()
        );
    }
}
