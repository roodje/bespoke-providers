package com.yolt.providers.amexgroup.amex.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.amexgroup.common.AmexGroupConfigurationProperties;
import com.yolt.providers.amexgroup.common.AmexGroupDataProviderV6;
import com.yolt.providers.amexgroup.common.auth.AmexGroupAuthMeanProducer;
import com.yolt.providers.amexgroup.common.auth.AmexGroupAuthMeanProducerV6;
import com.yolt.providers.amexgroup.common.mapper.AmexGroupDataMapperV5;
import com.yolt.providers.amexgroup.common.service.*;
import com.yolt.providers.amexgroup.common.utils.AmexMacHeaderUtilsV2;
import com.yolt.providers.common.versioning.ProviderVersion;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AmexBeanConfig {

    private static final String PROVIDER_IDENTIFIER = "AMEX";

    @Bean("AmexDataProviderV6")
    public AmexGroupDataProviderV6 getAmexDataProviderV6(AmexGroupConfigurationProperties properties,
                                                         @Qualifier("AmexGroupObjectMapper") final ObjectMapper objectMapper,
                                                         Clock clock) {
        AmexMacHeaderUtilsV2 amexMacHeaderUtils = new AmexMacHeaderUtilsV2();
        AmexGroupRestTemplateServiceV3 restTemplateService = new AmexGroupRestTemplateServiceV3(properties);
        AmexGroupAuthenticationService authenticationService = new AmexGroupAuthenticationServiceV3(restTemplateService, objectMapper, properties, amexMacHeaderUtils, clock);
        AmexGroupFetchDataService fetchDataService = new AmexGroupFetchDataServiceV5(restTemplateService, properties, amexMacHeaderUtils, new AmexGroupDataMapperV5(clock));
        AmexGroupAuthMeanProducer authMeanProducer = new AmexGroupAuthMeanProducerV6();
        return new AmexGroupDataProviderV6(properties, objectMapper, authenticationService, fetchDataService, authMeanProducer, PROVIDER_IDENTIFIER, ProviderVersion.VERSION_6);
    }
}
