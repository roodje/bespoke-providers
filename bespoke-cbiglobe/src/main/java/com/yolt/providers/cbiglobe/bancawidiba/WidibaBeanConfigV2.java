package com.yolt.providers.cbiglobe.bancawidiba;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.common.mapper.*;
import com.yolt.providers.cbiglobe.common.service.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class WidibaBeanConfigV2 {

    @Bean("WidibaAuthenticationServiceV2")
    public CbiGlobeAuthorizationService getAuthorizationService(WidibaProperties properties, Clock clock) {
        return new CbiGlobeAuthorizationService(properties, clock);
    }

    @Bean("WidibaConsentRequestServiceV2")
    public CbiGlobeConsentRequestServiceV4 getConsentRequestServiceV4(WidibaProperties properties, Clock clock) {
        return new CbiGlobeConsentRequestServiceV4(properties, clock, new CurrentAccountConsentAccessCreator());
    }

    @Bean("WidibaHttpClientFactoryV2")
    public CbiGlobeHttpClientFactory getHttpClientFactory(WidibaProperties properties,
                                                          @Qualifier("CbiGlobe") final ObjectMapper objectMapper) {
        return new CbiGlobeHttpClientFactory(properties, objectMapper);
    }

    @Bean("WidibaFetchServiceV2")
    public CbiGlobeFetchDataServiceV3 getFetchServiceV3(WidibaProperties properties, Clock clock) {
        CurrencyCodeMapper currencyCodeMapper = new CurrencyCodeMapperV1();
        CbiGlobeAccountMapper accountMapper = new CbiGlobeAccountMapperV2(new CbiGlobeBalanceMapperV2(currencyCodeMapper),
                new CbiGlobeTransactionMapperV2(currencyCodeMapper),
                new CbiGlobeExtendedAccountMapperV2(currencyCodeMapper),
                currencyCodeMapper,
                clock);
        return new CbiGlobeFetchDataServiceV3(properties, accountMapper, clock);
    }
}
