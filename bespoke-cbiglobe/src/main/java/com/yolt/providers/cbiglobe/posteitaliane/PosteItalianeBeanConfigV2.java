package com.yolt.providers.cbiglobe.posteitaliane;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.common.mapper.*;
import com.yolt.providers.cbiglobe.common.service.*;
import com.yolt.providers.cbiglobe.posteitaliane.common.service.PosteItalianeConsentRequestService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class PosteItalianeBeanConfigV2 {

    @Bean("PosteItalianeAuthenticationServiceV2")
    public CbiGlobeAuthorizationService getAuthorizationService(PosteItalianeProperties properties, Clock clock) {
        return new CbiGlobeAuthorizationService(properties, clock);
    }

    @Bean("PosteItalianeConsentRequestServiceV4")
    public CbiGlobeConsentRequestServiceV4 getConsentRequestServiceV4(PosteItalianeProperties properties, Clock clock) {
        return new PosteItalianeConsentRequestService(properties, clock, new CurrentAccountConsentAccessCreator());
    }

    @Bean("PosteItalianeHttpClientFactoryV2")
    public CbiGlobeHttpClientFactory getHttpClientFactory(PosteItalianeProperties properties,
                                                          @Qualifier("CbiGlobe") final ObjectMapper objectMapper) {
        return new CbiGlobeHttpClientFactory(properties, objectMapper);
    }

    @Bean("PosteItalianeFetchServiceV4")
    public CbiGlobeFetchDataServiceV3 getFetchServiceV3(PosteItalianeProperties properties, Clock clock) {
        CurrencyCodeMapper currencyCodeMapper = new CurrencyCodeMapperV1();
        CbiGlobeAccountMapper accountMapper = new CbiGlobeAccountMapperV2(new CbiGlobeBalanceMapperV2(currencyCodeMapper),
                new CbiGlobeTransactionMapperV2(currencyCodeMapper),
                new CbiGlobeExtendedAccountMapperV2(currencyCodeMapper),
                currencyCodeMapper,
                clock);
        return new CbiGlobeFetchDataServiceV3(properties, accountMapper, clock);
    }
}
