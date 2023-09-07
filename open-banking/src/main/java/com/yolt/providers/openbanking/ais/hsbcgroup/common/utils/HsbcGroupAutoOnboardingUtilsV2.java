package com.yolt.providers.openbanking.ais.hsbcgroup.common.utils;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
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

import static com.yolt.providers.openbanking.ais.hsbcgroup.common.auth.HsbcGroupAuthMeansBuilderV3.TRANSPORT_CERTIFICATE_NAME;
import static com.yolt.providers.openbanking.ais.hsbcgroup.common.auth.HsbcGroupAuthMeansBuilderV3.TRANSPORT_PRIVATE_KEY_ID_NAME;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HsbcGroupAutoOnboardingUtilsV2 {

    public static RestTemplate createAutoOnBoardingRestTemplate(final RestTemplateManager manager,
                                                                final Map<String, BasicAuthenticationMean> authenticationMeans,
                                                                final DefaultProperties properties,
                                                                final String provider) {
        UUID transportKeyId = UUID.fromString(authenticationMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME).getValue());
        X509Certificate clientCertificate = HsmUtils.getCertificate(authenticationMeans, TRANSPORT_CERTIFICATE_NAME, provider);
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