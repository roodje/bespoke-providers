package com.yolt.providers.openbanking.ais.newdaygroup.common.http;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders;
import com.yolt.providers.openbanking.ais.newdaygroup.common.auth.NewDayGroupAuthMeansBuilderV2;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class NewDayGroupOnboardingRestTemplateFactory {

    public static RestTemplate createAutoOnBoardingRestTemplate(final RestTemplateManager manager,
                                                                final Map<String, BasicAuthenticationMean> authenticationMeans,
                                                                final DefaultProperties properties,
                                                                final String provider) {
        UUID transportKeyId = UUID.fromString(authenticationMeans.get(NewDayGroupAuthMeansBuilderV2.TRANSPORT_KEY_ID).getValue());
        X509Certificate clientCertificate = HsmUtils.getCertificate(authenticationMeans, NewDayGroupAuthMeansBuilderV2.TRANSPORT_CERTIFICATE_NAME, provider);
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
