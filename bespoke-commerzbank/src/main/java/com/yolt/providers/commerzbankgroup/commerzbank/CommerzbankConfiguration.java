package com.yolt.providers.commerzbankgroup.commerzbank;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yolt.providers.commerzbankgroup.common.CommerzbankGroupUrlDataProvider;
import com.yolt.providers.commerzbankgroup.common.authentication.CommerzbankGroupAuthenticationService;
import com.yolt.providers.commerzbankgroup.common.authmeans.DefaultCommerzbankGroupAuthenticationMeansFactory;
import com.yolt.providers.commerzbankgroup.common.data.mapper.CommerzbankGroupDataMapperService;
import com.yolt.providers.commerzbankgroup.common.data.service.CommerzbankGroupFetchDataService;
import com.yolt.providers.common.providerdetail.AisDetailsProvider;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.versioning.ProviderVersion;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.Clock;

@Configuration
class CommerzbankConfiguration {

    public static final String PROVIDER_IDENTIFIER = "COMMERZBANK";

    @Bean
    @Qualifier("Commerzbank")
    public ObjectMapper getCommerzbankMapper(Jackson2ObjectMapperBuilder mapperBuilder) {
        ObjectMapper mapper = mapperBuilder.build();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean("CommerzbankProvider")
    public UrlDataProvider commerzbankProvider(@Qualifier("Commerzbank") final ObjectMapper objectMapper,
                                               final MeterRegistry meterRegistry,
                                               final CommerzbankProperties properties,
                                               final Clock clock) {
        CommerzbankGroupDataMapperService commerzbankGroupDataMapperService = new CommerzbankGroupDataMapperService(clock);
        return new CommerzbankGroupUrlDataProvider(new DefaultCommerzbankGroupAuthenticationMeansFactory(PROVIDER_IDENTIFIER),
                ProviderVersion.VERSION_1, PROVIDER_IDENTIFIER, "Commerzbank AG", properties,
                meterRegistry, objectMapper, clock,
                new CommerzbankGroupAuthenticationService(clock),
                new CommerzbankGroupFetchDataService(commerzbankGroupDataMapperService));
    }

    @Bean
    AisDetailsProvider getAisSiteDetails() {
        return new CommerzbankSiteDetailsProvider();
    }
}
