package com.yolt.providers.ing.de.config;

import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.ing.common.IngDataProviderV9;
import com.yolt.providers.ing.common.config.IngObjectMapper;
import com.yolt.providers.ing.common.service.*;
import com.yolt.providers.ing.de.service.IngDeDataMapperService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class IngDeBeanConfig {

    @Bean("IngDeDataProviderV10")
    public IngDataProviderV9 getDataProviderV10(IngDeProperties properties, Clock clock) {
        IngClientAwareRestTemplateService restTemplateService = new IngClientAwareRestTemplateService(properties);
        IngSigningUtil ingSigningUtil = new IngSigningUtil();
        IngFetchDataService fetchDataService = new IngFetchDataServiceV9(new IngDataMapperServiceV6(IngDeProperties.ZONE_ID), restTemplateService, ingSigningUtil, properties, clock);
        IngAuthenticationServiceV3 authenticationService = new IngAuthenticationServiceV3(restTemplateService, ingSigningUtil, properties, IngDeProperties.COUNTRY_CODE, clock);
        return new IngDataProviderV9(
                fetchDataService,
                authenticationService,
                IngObjectMapper.get(),
                IngDeProperties.PROVIDER_IDENTIFIER,
                IngDeProperties.PROVIDER_IDENTIFIER_DISPLAY_NAME,
                ProviderVersion.VERSION_10,
                clock);
    }

    @Bean("IngDeDataProviderV11")
    public IngDataProviderV9 getDataProviderV11(IngDeProperties properties, Clock clock) {
        IngClientAwareRestTemplateService restTemplateService = new IngClientAwareRestTemplateService(properties);
        IngSigningUtil ingSigningUtil = new IngSigningUtil();
        IngFetchDataService fetchDataService = new IngFetchDataServiceV9(new IngDeDataMapperService(IngDeProperties.ZONE_ID), restTemplateService, ingSigningUtil, properties, clock);
        IngAuthenticationServiceV3 authenticationService = new IngAuthenticationServiceV3(restTemplateService, ingSigningUtil, properties, IngDeProperties.COUNTRY_CODE, clock);
        return new IngDataProviderV9(
                fetchDataService,
                authenticationService,
                IngObjectMapper.get(),
                IngDeProperties.PROVIDER_IDENTIFIER,
                IngDeProperties.PROVIDER_IDENTIFIER_DISPLAY_NAME,
                ProviderVersion.VERSION_11,
                clock);
    }
}
