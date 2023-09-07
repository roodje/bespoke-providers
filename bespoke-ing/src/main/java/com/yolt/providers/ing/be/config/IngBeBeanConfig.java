package com.yolt.providers.ing.be.config;

import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.ing.common.IngDataProviderV9;
import com.yolt.providers.ing.common.config.IngObjectMapper;
import com.yolt.providers.ing.common.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class IngBeBeanConfig {

    @Bean("IngBeDataProviderV10")
    public IngDataProviderV9 getDataProviderV10(IngBeProperties properties, Clock clock) {
        IngClientAwareRestTemplateService restTemplateService = new IngClientAwareRestTemplateService(properties);
        IngSigningUtil ingSigningUtil = new IngSigningUtil();
        IngFetchDataService fetchDataService = new IngFetchDataServiceV9(new IngDataMapperServiceV6(IngBeProperties.ZONE_ID), restTemplateService, ingSigningUtil, properties, clock);
        IngAuthenticationServiceV3 authenticationService = new IngAuthenticationServiceV3(restTemplateService, ingSigningUtil, properties, IngBeProperties.COUNTRY_CODE, clock);
        return new IngDataProviderV9(
                fetchDataService,
                authenticationService,
                IngObjectMapper.get(),
                IngBeProperties.PROVIDER_IDENTIFIER,
                IngBeProperties.PROVIDER_IDENTIFIER_DISPLAY_NAME,
                ProviderVersion.VERSION_10,
                clock);
    }
}
