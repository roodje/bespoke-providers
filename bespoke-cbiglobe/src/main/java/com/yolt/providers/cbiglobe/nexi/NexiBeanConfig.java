package com.yolt.providers.cbiglobe.nexi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.common.mapper.*;
import com.yolt.providers.cbiglobe.common.service.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class NexiBeanConfig {

    @Bean("NexiAuthenticationServiceV1")
    public CbiGlobeAuthorizationService getAuthorizationServiceV1(NexiProperties properties, Clock clock) {
        return new CbiGlobeAuthorizationService(properties, clock);
    }

    @Bean("NexiConsentRequestServiceV1")
    public CbiGlobeConsentRequestServiceV4 getConsentRequestServiceV1(NexiProperties properties, Clock clock) {
        return new CbiGlobeConsentRequestServiceV4(properties, clock, new CreditCardAccountConsentAccessCreator());
    }

    @Bean("NexiHttpClientFactoryV1")
    public CbiGlobeHttpClientFactory getHttpClientFactoryV1(NexiProperties properties,
                                                            @Qualifier("CbiGlobe") final ObjectMapper objectMapper) {
        return new CbiGlobeHttpClientFactory(properties, objectMapper);
    }

    @Bean("NexiFetchServiceV1")
    public CbiGlobeFetchDataService getFetchServiceV1(NexiProperties properties, Clock clock) {
        CurrencyCodeMapper currencyCodeMapper = new CurrencyCodeMapperV1();
        CbiGlobeCardAccountMapper accountMapper = new CbiGlobeCardAccountMapperV1(
                new CbiGlobeCardBalanceMapperV1(currencyCodeMapper),
                new CbiGlobeCardTransactionMapperV1(currencyCodeMapper),
                new CbiGlobeExtendedCardAccountMapperV1(currencyCodeMapper),
                currencyCodeMapper,
                clock);
        return new CbiGlobeFetchCardDataService(properties, accountMapper, clock);
    }
}
