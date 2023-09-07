package com.yolt.providers.cbiglobe.bpm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.common.mapper.*;
import com.yolt.providers.cbiglobe.common.service.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class BpmBeanConfigV2 {

    @Bean("BpmAuthenticationServiceV2")
    public CbiGlobeAuthorizationService getAuthorizationService(BpmProperties properties, Clock clock) {
        return new CbiGlobeAuthorizationService(properties, clock);
    }

    @Bean("BpmConsentRequestServiceV4")
    public CbiGlobeConsentRequestServiceV4 getConsentRequestServiceV4(BpmProperties properties, Clock clock) {
        return new CbiGlobeConsentRequestServiceV4(properties, clock, new CurrentAccountConsentAccessCreator());
    }

    @Bean("BpmHttpClientFactoryV2")
    public CbiGlobeHttpClientFactory getHttpClientFactory(BpmProperties properties,
                                                          @Qualifier("CbiGlobe") final ObjectMapper objectMapper) {
        return new CbiGlobeHttpClientFactory(properties, objectMapper);
    }

    @Bean("BpmFetchServiceV4")
    public CbiGlobeFetchDataServiceV3 getFetchServiceV3(BpmProperties properties, Clock clock) {
        CurrencyCodeMapper currencyCodeMapper = new CurrencyCodeMapperV1();
        CbiGlobeAccountMapper accountMapper = new BpmAccountMapperV1(
                new CbiGlobeBalanceMapperV2(currencyCodeMapper),
                new CbiGlobeTransactionMapperV2(currencyCodeMapper),
                new CbiGlobeExtendedAccountMapperV2(currencyCodeMapper),
                currencyCodeMapper,
                clock);
        return new CbiGlobeFetchDataServiceV3(properties, accountMapper, clock);
    }
}
