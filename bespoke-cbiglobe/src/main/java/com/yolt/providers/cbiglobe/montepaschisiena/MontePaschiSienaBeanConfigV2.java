package com.yolt.providers.cbiglobe.montepaschisiena;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.common.mapper.*;
import com.yolt.providers.cbiglobe.common.service.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class MontePaschiSienaBeanConfigV2 {

    @Bean("MontePaschiSienaAuthenticationServiceV2")
    public CbiGlobeAuthorizationService getAuthorizationService(MontePaschiSienaProperties properties, Clock clock) {
        return new CbiGlobeAuthorizationService(properties, clock);
    }

    @Bean("MontePaschiSienaConsentRequestServiceV4")
    public CbiGlobeConsentRequestServiceV4 getConsentRequestServiceV4(MontePaschiSienaProperties properties, Clock clock) {
        return new CbiGlobeConsentRequestServiceV4(properties, clock, new CurrentAccountConsentAccessCreator());
    }

    @Bean("MontePaschiSienaHttpClientFactoryV2")
    public CbiGlobeHttpClientFactory getHttpClientFactory(MontePaschiSienaProperties properties,
                                                          @Qualifier("CbiGlobe") final ObjectMapper objectMapper) {
        return new CbiGlobeHttpClientFactory(properties, objectMapper);
    }

    @Bean("MontePaschiSienaFetchServiceV4")
    public CbiGlobeFetchDataServiceV3 getFetchServiceV3(MontePaschiSienaProperties properties, Clock clock) {
        CurrencyCodeMapper currencyCodeMapper = new CurrencyCodeMapperV1();
        CbiGlobeAccountMapper accountMapper = new CbiGlobeAccountMapperV2(new CbiGlobeBalanceMapperV2(currencyCodeMapper),
                new CbiGlobeTransactionMapperV2(currencyCodeMapper),
                new CbiGlobeExtendedAccountMapperV2(currencyCodeMapper),
                currencyCodeMapper,
                clock);
        return new CbiGlobeFetchDataServiceV3(properties, accountMapper, clock);
    }
}
