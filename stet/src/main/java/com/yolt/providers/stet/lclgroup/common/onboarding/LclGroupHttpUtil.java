package com.yolt.providers.stet.lclgroup.common.onboarding;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.stet.lclgroup.common.auth.LclGroupClientConfiguration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class LclGroupHttpUtil {

    static RestTemplate createRestTemplateWithManagedMutualTLSTemplate(RestTemplateManager manager,
                                                                       LclGroupClientConfiguration clientConfiguration,
                                                                       String baseUrl) {
        UUID transportKeyId = UUID.fromString(clientConfiguration.getClientTransportKeyId().toString());
        X509Certificate transportCertificate = clientConfiguration.getClientTransportCertificate();

        return manager.manage(transportKeyId, new X509Certificate[]{transportCertificate}, externalRestTemplateBuilderFactory ->
                externalRestTemplateBuilderFactory
                        .rootUri(baseUrl)
                        .messageConverters(Arrays.asList(
                                new FormHttpMessageConverter(),
                                LclLocalDateJsonDeserializer.customizedJacksonMapper(),
                                new ByteArrayHttpMessageConverter()))
                        .build()
        );
    }
}
