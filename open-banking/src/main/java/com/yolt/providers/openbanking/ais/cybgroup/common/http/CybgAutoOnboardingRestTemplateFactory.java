package com.yolt.providers.openbanking.ais.cybgroup.common.http;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.cybgroup.common.auth.CybgGroupAuthMeansBuilderV2;
import com.yolt.providers.openbanking.ais.cybgroup.common.config.CybgGroupPropertiesV2;
import com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CybgAutoOnboardingRestTemplateFactory {

    public static RestTemplate createAutoOnBoardingRestTemplate(final RestTemplateManager manager,
                                                                final Map<String, BasicAuthenticationMean> authenticationMeans,
                                                                final CybgGroupPropertiesV2 properties,
                                                                final String provider) {
        final UUID transportKeyId = UUID.fromString(authenticationMeans.get(CybgGroupAuthMeansBuilderV2.TRANSPORT_PRIVATE_KEY_ID_NAME).getValue());
        final X509Certificate clientCertificate = HsmUtils.getCertificate(authenticationMeans, CybgGroupAuthMeansBuilderV2.TRANSPORT_CERTIFICATE_NAME, provider);
        return manager.manage(new RestTemplateManagerConfiguration(transportKeyId, clientCertificate, externalRestTemplateBuilderFactory ->
                externalRestTemplateBuilderFactory
                        .rootUri(properties.getBaseUrl())
                        .externalTracing(HttpExtraHeaders.INTERACTION_ID_HEADER_NAME)
                        .additionalMessageConverters(
                        new FormHttpMessageConverter(),
                        new MappingJackson2HttpMessageConverter(),
                        new StringHttpMessageConverter())
                        .build()));
    }
}
