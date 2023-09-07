package com.yolt.providers.redsys.evo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.redsys.common.rest.BookingStatus;
import com.yolt.providers.redsys.common.rest.RedsysRestTemplateService;
import com.yolt.providers.redsys.common.service.RedsysAllAccountsConsentObjectService;
import com.yolt.providers.redsys.common.service.RedsysAuthorizationService;
import com.yolt.providers.redsys.common.service.RedsysFetchDataServiceV2;
import com.yolt.providers.redsys.common.service.TransactionsFetchStartTimeNoLimited;
import com.yolt.providers.redsys.common.service.mapper.CurrencyCodeMapper;
import com.yolt.providers.redsys.common.service.mapper.CurrencyCodeMapperV1;
import com.yolt.providers.redsys.common.service.mapper.RedsysDataMapperServiceV3;
import com.yolt.providers.redsys.evo.service.mapper.EvoExtendedDataMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class EvoBeanConfig {

    private static final ZoneId MADRID_ZONE_ID = ZoneId.of("Europe/Madrid");
    private static final String TRANSACTION_DESCRIPTION_FALLBACK = "Description not available";

    @Bean
    public EvoDataProvider getEvoDataProvider(final EvoProperties properties,
                                           @Qualifier("Redsys") final ObjectMapper mapper,
                                           Clock clock) {
        RedsysAuthorizationService authorizationService = getAuthorizationService(mapper, properties);
        RedsysFetchDataServiceV2 fetchDataService = getFetchDataService(mapper, properties, clock);
        return new EvoDataProvider(properties, authorizationService, fetchDataService, mapper, clock);
    }

    private RedsysAuthorizationService getAuthorizationService(final ObjectMapper objectMapper,
                                                               final EvoProperties properties) {
        return new RedsysAuthorizationService(new RedsysRestTemplateService(objectMapper, properties),
                new RedsysAllAccountsConsentObjectService(),
                properties);
    }

    private RedsysFetchDataServiceV2 getFetchDataService(final ObjectMapper objectMapper,
                                                         final EvoProperties properties,
                                                         final Clock clock) {
        CurrencyCodeMapper currencyCodeMapper = new CurrencyCodeMapperV1();
        return new RedsysFetchDataServiceV2(new EvoRestTemplateService(objectMapper, properties, clock), properties,
                new RedsysDataMapperServiceV3(
                        currencyCodeMapper,
                        new EvoExtendedDataMapper(currencyCodeMapper, MADRID_ZONE_ID),
                        clock,
                        TRANSACTION_DESCRIPTION_FALLBACK),
                BookingStatus.BOTH, new TransactionsFetchStartTimeNoLimited());
    }
}
