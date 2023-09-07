package com.yolt.providers.bunq.beanconfig;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bunq.common.configuration.BunqProperties;
import com.yolt.providers.bunq.common.http.BunqHttpClientFactory;
import com.yolt.providers.bunq.common.http.BunqHttpHeaderProducer;
import com.yolt.providers.bunq.common.service.authorization.BunqAuthorizationServiceV5;
import com.yolt.providers.bunq.common.service.autoonboarding.BunqAutoOnboardingServiceV2;
import com.yolt.providers.bunq.common.service.fetchdata.BunqAccountsAndTransactionsServiceV5;
import com.yolt.providers.bunq.common.util.BunqCertificateFormatter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.Clock;

@Configuration
public class BunqBeanConfig {

    @Bean("BunqObjectMapper")
    public ObjectMapper getObjectMapper(final Jackson2ObjectMapperBuilder mapperBuilder) {
        ObjectMapper mapper = mapperBuilder.build();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Bean("BunqHttpClientFactory")
    public BunqHttpClientFactory getBunqHttpClientFactory(MeterRegistry meterRegistry,
                                                          @Qualifier("BunqObjectMapper") ObjectMapper objectMapper,
                                                          BunqProperties properties) {
        return new BunqHttpClientFactory(meterRegistry, objectMapper, properties, new BunqHttpHeaderProducer(objectMapper));
    }

    @Bean("BunqAccountsAndTransactionsServiceV5")
    public BunqAccountsAndTransactionsServiceV5 getBunqAccountsAndTransactionsServiceV5(Clock clock) {
        return new BunqAccountsAndTransactionsServiceV5(clock);
    }

    @Bean("BunqAuthorizationServiceV5")
    public BunqAuthorizationServiceV5 getBunqAuthorizationServiceV5(BunqProperties properties) {
        return new BunqAuthorizationServiceV5(properties);
    }

    @Bean("BunqAutoonboardingServiceV2")
    public BunqAutoOnboardingServiceV2 getBunqAutoonboardingServiceV2() {
        return new BunqAutoOnboardingServiceV2(new BunqCertificateFormatter());
    }
}
