package com.yolt.providers.redsys.laboralkutxa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.redsys.common.rest.BookingStatus;
import com.yolt.providers.redsys.common.rest.RedsysRestTemplateService;
import com.yolt.providers.redsys.common.service.RedsysAllAccountsConsentObjectService;
import com.yolt.providers.redsys.common.service.RedsysAuthorizationService;
import com.yolt.providers.redsys.common.service.RedsysFetchDataServiceV2;
import com.yolt.providers.redsys.common.service.TransactionFetchStartTimeDaysLimited;
import com.yolt.providers.redsys.common.service.mapper.CurrencyCodeMapper;
import com.yolt.providers.redsys.common.service.mapper.CurrencyCodeMapperV1;
import com.yolt.providers.redsys.common.service.mapper.RedsysDataMapperServiceV3;
import com.yolt.providers.redsys.common.service.mapper.RedsysExtendedDataMapperV2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;

@Configuration
public class LaboralKutxaBeanConfig {

    private static final ZoneId MADRID_ZONE_ID = ZoneId.of("Europe/Madrid");
    private static final String TRANSACTION_DESCRIPTION_FALLBACK = "Description not available";

    @Bean
    public LaboralKutxaDataProvider getLaboralKutxaDataProvider(final LaboralKutxaProperties properties,
                                                    @Qualifier("Redsys") final ObjectMapper mapper,
                                                    Clock clock) {
        RedsysAuthorizationService authorizationService = getAuthorizationService(mapper, properties);
        RedsysFetchDataServiceV2 fetchDataService = getFetchDataService(mapper, properties, clock);
        return new LaboralKutxaDataProvider(properties, authorizationService, fetchDataService, mapper, clock);
    }

    private RedsysAuthorizationService getAuthorizationService(final ObjectMapper objectMapper,
                                                               final LaboralKutxaProperties properties) {
        return new RedsysAuthorizationService(new RedsysRestTemplateService(objectMapper, properties),
                new RedsysAllAccountsConsentObjectService(),
                properties);
    }

    private RedsysFetchDataServiceV2 getFetchDataService(final ObjectMapper objectMapper,
                                                         final LaboralKutxaProperties properties,
                                                         final Clock clock) {
        CurrencyCodeMapper currencyCodeMapper = new CurrencyCodeMapperV1();
        return new RedsysFetchDataServiceV2(new RedsysRestTemplateService(objectMapper, properties), properties,
                new RedsysDataMapperServiceV3(
                        currencyCodeMapper,
                        new RedsysExtendedDataMapperV2(currencyCodeMapper, MADRID_ZONE_ID),
                        clock,
                        TRANSACTION_DESCRIPTION_FALLBACK),
                BookingStatus.BOTH, new TransactionFetchStartTimeDaysLimited(Duration.ofDays(364), clock));
    }
}
