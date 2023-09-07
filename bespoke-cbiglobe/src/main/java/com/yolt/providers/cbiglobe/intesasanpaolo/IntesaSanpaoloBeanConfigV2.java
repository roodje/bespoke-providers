package com.yolt.providers.cbiglobe.intesasanpaolo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.common.mapper.*;
import com.yolt.providers.cbiglobe.common.service.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class IntesaSanpaoloBeanConfigV2 {

    @Bean("IntesaSanpaoloAuthenticationServiceV2")
    public CbiGlobeAuthorizationService getAuthorizationService(IntesaSanpaoloProperties properties, Clock clock) {
        return new CbiGlobeAuthorizationService(properties, clock);
    }

    @Bean("IntesaSanpaoloConsentRequestServiceV4")
    public CbiGlobeConsentRequestServiceV4 getConsentRequestServiceV4(IntesaSanpaoloProperties properties, Clock clock) {
        return new CbiGlobeConsentRequestServiceV4(properties, clock, new CurrentAccountConsentAccessCreator());
    }

    @Bean("IntesaSanpaoloHttpClientFactoryV2")
    public CbiGlobeHttpClientFactory getHttpClientFactory(IntesaSanpaoloProperties properties,
                                                          @Qualifier("CbiGlobe") final ObjectMapper objectMapper) {
        return new CbiGlobeHttpClientFactory(properties, objectMapper);
    }

    @Bean("IntesaSanpaoloFetchServiceV4")
    public CbiGlobeFetchDataServiceV3 getFetchServiceV3(IntesaSanpaoloProperties properties, Clock clock) {
        CurrencyCodeMapper currencyCodeMapper = new CurrencyCodeMapperV1();
        CbiGlobeAccountMapper accountMapper = new CbiGlobeAccountMapperV2(new CbiGlobeBalanceMapperV2(currencyCodeMapper),
                new CbiGlobeTransactionMapperV2(currencyCodeMapper),
                new CbiGlobeExtendedAccountMapperV2(currencyCodeMapper),
                currencyCodeMapper,
                clock);
        return new CbiGlobeFetchDataServiceV3(properties, accountMapper, clock);
    }
}
