package com.yolt.providers.redsys.cajamarcajarural;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.redsys.common.rest.BookingStatus;
import com.yolt.providers.redsys.common.rest.RedsysRestTemplateService;
import com.yolt.providers.redsys.common.service.*;
import com.yolt.providers.redsys.common.service.mapper.CurrencyCodeMapper;
import com.yolt.providers.redsys.common.service.mapper.CurrencyCodeMapperV1;
import com.yolt.providers.redsys.common.service.mapper.RedsysDataMapperServiceV3;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class CajamarCajaRuralBeanConfig {

    private static final ZoneId MADRID_ZONE_ID = ZoneId.of("Europe/Madrid");
    private static final String TRANSACTION_DESCRIPTION_FALLBACK = "Description not available";

    @Bean
    public CajamarCajaRuralDataProviderV1 getCajamarCajaRuralDataProvider(final CajamarCajaRuralProperties properties,
                                                        @Qualifier("Redsys") final ObjectMapper mapper,
                                                        Clock clock) {
        RedsysAuthorizationService authorizationService = getAuthorizationService(mapper, properties);
        RedsysFetchDataServiceV3 fetchDataService = getFetchDataService(mapper, properties, clock);
        return new CajamarCajaRuralDataProviderV1(properties, authorizationService, fetchDataService, mapper, clock);
    }

    private RedsysAuthorizationService getAuthorizationService(final ObjectMapper objectMapper,
                                                               final CajamarCajaRuralProperties properties) {
        return new RedsysAuthorizationService(new CajamarCajaRuralRestTemplateService(objectMapper, properties),
                new RedsysAllAccountsConsentObjectService(),
                properties);
    }

    private RedsysFetchDataServiceV3 getFetchDataService(final ObjectMapper objectMapper,
                                                           final CajamarCajaRuralProperties properties,
                                                           final Clock clock) {
        CurrencyCodeMapper currencyCodeMapper = new CurrencyCodeMapperV1();
        return new RedsysFetchDataServiceV3(new RedsysRestTemplateService(objectMapper, properties), properties,
                new RedsysDataMapperServiceV3(
                        currencyCodeMapper,
                        new CajamarCajaRuralExtendedDataMapper(currencyCodeMapper, MADRID_ZONE_ID),
                        clock,
                        TRANSACTION_DESCRIPTION_FALLBACK),
                BookingStatus.BOOKED, new TransactionsFetchStartTimeNoLimited());
    }
}
