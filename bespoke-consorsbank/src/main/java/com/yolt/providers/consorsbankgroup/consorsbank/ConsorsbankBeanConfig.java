package com.yolt.providers.consorsbankgroup.consorsbank;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.consorsbankgroup.common.ais.AccessMeansMapper;
import com.yolt.providers.consorsbankgroup.common.ais.ConsorsbankGroupDataProvider;
import com.yolt.providers.consorsbankgroup.common.ais.HsmEidasUtils;
import com.yolt.providers.consorsbankgroup.common.ais.http.DefaultRestClient;
import com.yolt.providers.consorsbankgroup.common.ais.http.HttpClientFactory;
import com.yolt.providers.consorsbankgroup.common.ais.mapper.*;
import com.yolt.providers.consorsbankgroup.common.ais.service.DefaultAuthorizationService;
import com.yolt.providers.consorsbankgroup.common.ais.service.DefaultFetchDataService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Map;

import static com.yolt.providers.consorsbankgroup.common.ais.service.DefaultAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_NAME;
import static com.yolt.providers.consorsbankgroup.common.ais.service.DefaultAuthenticationMeans.CLIENT_TRANSPORT_KEY_ID_NAME;

@Configuration
public class ConsorsbankBeanConfig {

    public static final String PROVIDER_IDENTIFIER = "CONSORSBANK";
    public static final String PROVIDER_IDENTIFIER_DISPLAY_NAME = "Consorsbank";
    public static final String ACCOUNT_NAME = "Consorsbank account";
    public static final ZoneId ZONE_ID = ZoneId.of("Europe/Berlin");

    @Bean("consorsbankDataProviderV1")
    public ConsorsbankGroupDataProvider getConsorsbankDataProviderV1(final HttpClientFactory httpClientFactory,
                                                                     final Clock clock) {
        Clock clockWithZone = clock.withZone(ZONE_ID);

        return new ConsorsbankGroupDataProvider(
                PROVIDER_IDENTIFIER,
                PROVIDER_IDENTIFIER_DISPLAY_NAME,
                ProviderVersion.VERSION_1,
                clockWithZone,
                getTypedAuthMeans(),
                HsmEidasUtils.getKeyRequirements(CLIENT_TRANSPORT_KEY_ID_NAME, CLIENT_TRANSPORT_CERTIFICATE_NAME),
                new AccessMeansMapper(getConsorsbankGroupObjectMapper()),
                httpClientFactory,
                new DefaultAuthorizationService(new DefaultRestClient(), clock),
                new DefaultFetchDataService(
                        new DefaultAccountMapper(
                                new DefaultExtendedModelAccountMapper(),
                                ACCOUNT_NAME,
                                clockWithZone),
                        new DefaultBalanceMapper(clockWithZone),
                        new DefaultTransactionMapper(new DefaultExtendedModelTransactionMapper()),
                        new DefaultRestClient()));
    }

    @Bean("consorsbankHttpClientFactoryV1")
    public HttpClientFactory getConsorsbankHttpClilentFactoryV1(final ConsorsbankProperties properties,
                                                                final MeterRegistry meterRegistry) {
        return new HttpClientFactory(getConsorsbankGroupObjectMapper(), meterRegistry, properties);
    }

    private static Map<String, TypedAuthenticationMeans> getTypedAuthMeans() {
        return Map.ofEntries(
                Map.entry(CLIENT_TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID),
                Map.entry(CLIENT_TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM)
        );
    }

    @Bean
    @Qualifier("consorsbankGroupObjectMapper")
    public ObjectMapper getConsorsbankGroupObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule(new JavaTimeModule());

        return mapper;
    }
}
