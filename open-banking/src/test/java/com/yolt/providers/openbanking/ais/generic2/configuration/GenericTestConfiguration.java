package com.yolt.providers.openbanking.ais.generic2.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.oauth2.implementations.DefaultClientSecretBasicOauth2Client;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import io.micrometer.core.instrument.MeterRegistry;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class GenericTestConfiguration {

    @Bean
    public DefaultProperties getDefaultProperties() {
        return new GenericTestProperties();
    }

    @Bean
    public DefaultAuthenticationService getAuthenticationService(final DefaultProperties properties, final Clock clock) {
        return new DefaultAuthenticationService(properties.getOAuthAuthorizationUrl(),
                new DefaultClientSecretBasicOauth2Client(properties, false),
                new ExternalUserRequestTokenSigner(AlgorithmIdentifiers.RSA_PSS_USING_SHA256),
                new DefaultTokenClaimsProducer(new DefaultJwtClaimsProducer((defaultAuthMeans) -> null, properties.getAudience())),
                clock);
    }

    @Bean
    public HttpClientFactory getHttpClientFactory(final DefaultProperties properties,
                                                  final MeterRegistry registry,
                                                  final @Qualifier("OpenBanking") ObjectMapper mapper) {
        return new DefaultHttpClientFactory(properties, registry, mapper);
    }
}
