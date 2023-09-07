package com.yolt.providers.openbanking.ais.vanquisgroup.common.http;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.auth.VanquisGroupAuthMeansBuilderV2;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.properties.VanquisGroupPropertiesV2;
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
public class VanquisGroupAutoOboardingRestTemplateFactory {

    public static RestTemplate createAutoOnBoardingRestTemplate(final RestTemplateManager manager,
                                                                final Map<String, BasicAuthenticationMean> authenticationMeans,
                                                                final VanquisGroupPropertiesV2 properties,
                                                                final String provider) {
        UUID transportKeyId = UUID.fromString(authenticationMeans.get(VanquisGroupAuthMeansBuilderV2.TRANSPORT_PRIVATE_KEY_ID_NAME).getValue());
        X509Certificate clientCertificate = HsmUtils.getCertificate(authenticationMeans, VanquisGroupAuthMeansBuilderV2.TRANSPORT_CERTIFICATE_NAME, provider);
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