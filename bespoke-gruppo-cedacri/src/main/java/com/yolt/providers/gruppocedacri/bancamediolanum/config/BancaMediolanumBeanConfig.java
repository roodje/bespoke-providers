package com.yolt.providers.gruppocedacri.bancamediolanum.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.gruppocedacri.common.GruppoCedacriDataProviderV1;
import com.yolt.providers.gruppocedacri.common.autoonboarding.GruppoCedacriAutoOnboardingService;
import com.yolt.providers.gruppocedacri.common.config.ProviderIdentification;
import com.yolt.providers.gruppocedacri.common.http.GruppoCedacriHttpClientFactory;
import com.yolt.providers.gruppocedacri.common.http.GruppoCedacriHttpClientFactoryV1;
import com.yolt.providers.gruppocedacri.common.mapper.GruppoCedacriAccountMapper;
import com.yolt.providers.gruppocedacri.common.mapper.GruppoCedacriAccountMapperV1;
import com.yolt.providers.gruppocedacri.common.mapper.GruppoCedacriTransactionMapper;
import com.yolt.providers.gruppocedacri.common.mapper.GruppoCedacriTransactionMapperV1;
import com.yolt.providers.gruppocedacri.common.service.GruppoCedacriAuthorizationService;
import com.yolt.providers.gruppocedacri.common.service.GruppoCedacriAuthorizationServiceV1;
import com.yolt.providers.gruppocedacri.common.service.GruppoCedacriFetchDataService;
import com.yolt.providers.gruppocedacri.common.service.GruppoCedacriFetchDataServiceV1;
import com.yolt.providers.gruppocedacri.common.util.GruppoCedacriDateConverter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;
import static com.yolt.providers.gruppocedacri.bancamediolanum.config.BancaMediolanumDetailsProvider.DISPLAY_NAME;
import static com.yolt.providers.gruppocedacri.bancamediolanum.config.BancaMediolanumDetailsProvider.PROVIDER_KEY;

@Configuration
public class BancaMediolanumBeanConfig {

    @Bean("BancaMediolanumDataProviderV1")
    public GruppoCedacriDataProviderV1 getBancaMediolanumDataProviderV1(@Qualifier("GruppoCedacri") ObjectMapper objectMapper,
                                                                        BancaMediolanumProperties properties,
                                                                        MeterRegistry meterRegistry,
                                                                        Clock clock) {
        ProviderIdentification providerIdentification = new ProviderIdentification(PROVIDER_KEY, DISPLAY_NAME, VERSION_1);
        GruppoCedacriAuthorizationService authorizationService = getAuthenticationServiceV1(clock, objectMapper);
        GruppoCedacriFetchDataService fetchDataService = getFetchDataServiceV1(clock, properties);
        GruppoCedacriHttpClientFactory httpClientFactory = getHttpClientFactoryV1(objectMapper, properties, meterRegistry);
        GruppoCedacriAutoOnboardingService autoOnboardingService = new GruppoCedacriAutoOnboardingService();

        return new GruppoCedacriDataProviderV1(providerIdentification,
                authorizationService,
                fetchDataService,
                autoOnboardingService,
                httpClientFactory,
                objectMapper,
                clock);
    }

    private GruppoCedacriAuthorizationService getAuthenticationServiceV1(Clock clock,
                                                                         @Qualifier("GruppoCedacri") ObjectMapper objectMapper) {
        return new GruppoCedacriAuthorizationServiceV1(clock, objectMapper);
    }

    private GruppoCedacriFetchDataService getFetchDataServiceV1(Clock clock,
                                                                BancaMediolanumProperties properties) {
        GruppoCedacriDateConverter dateConverter = new GruppoCedacriDateConverter(clock, ZoneId.of("Europe/Rome"));
        GruppoCedacriTransactionMapper transactionMapper = new GruppoCedacriTransactionMapperV1(dateConverter);
        GruppoCedacriAccountMapper accountMapper = new GruppoCedacriAccountMapperV1(dateConverter);
        return new GruppoCedacriFetchDataServiceV1(accountMapper, transactionMapper, dateConverter, properties);
    }

    private GruppoCedacriHttpClientFactory getHttpClientFactoryV1(ObjectMapper objectMapper,
                                                                  BancaMediolanumProperties properties,
                                                                  MeterRegistry meterRegistry) {
        return new GruppoCedacriHttpClientFactoryV1(objectMapper, meterRegistry, properties);
    }
}
