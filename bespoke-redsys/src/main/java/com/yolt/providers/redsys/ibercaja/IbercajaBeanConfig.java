package com.yolt.providers.redsys.ibercaja;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.redsys.common.rest.BookingStatus;
import com.yolt.providers.redsys.common.rest.RedsysRestTemplateService;
import com.yolt.providers.redsys.common.service.RedsysAllAccountsConsentObjectService;
import com.yolt.providers.redsys.common.service.RedsysAuthorizationService;
import com.yolt.providers.redsys.common.service.RedsysFetchDataServiceV3;
import com.yolt.providers.redsys.common.service.TransactionsFetchStartTimeNoLimited;
import com.yolt.providers.redsys.common.service.mapper.CurrencyCodeMapper;
import com.yolt.providers.redsys.common.service.mapper.CurrencyCodeMapperV1;
import com.yolt.providers.redsys.common.service.mapper.RedsysDataMapperServiceV3;
import com.yolt.providers.redsys.common.service.mapper.RedsysExtendedDataMapperV2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class IbercajaBeanConfig {

    private static final ZoneId MADRID_ZONE_ID = ZoneId.of("Europe/Madrid");
    private static final String TRANSACTION_DESCRIPTION_FALLBACK = "Description not available";

    @Bean("IbercajaDataProvider")
    public IbercajaDataProvider getDataProvider(IbercajaProperties properties,
                                                @Qualifier("Redsys") final ObjectMapper objectMapper,
                                                Clock clock) {
        return new IbercajaDataProvider(properties,
                getAuthorizationService(objectMapper, properties),
                getFetchDataServiceV2(objectMapper, properties, clock),
                objectMapper,
                clock);
    }

    public RedsysAuthorizationService getAuthorizationService(final ObjectMapper objectMapper,
                                                              final IbercajaProperties properties) {
        return new RedsysAuthorizationService(new RedsysRestTemplateService(objectMapper, properties),
                new RedsysAllAccountsConsentObjectService(),
                properties);
    }

    public RedsysFetchDataServiceV3 getFetchDataServiceV2(final ObjectMapper objectMapper,
                                                          final IbercajaProperties properties,
                                                          Clock clock) {
        CurrencyCodeMapper currencyCodeMapper = new CurrencyCodeMapperV1();
        return new RedsysFetchDataServiceV3(new RedsysRestTemplateService(objectMapper, properties), properties,
                new RedsysDataMapperServiceV3(
                        currencyCodeMapper,
                        new RedsysExtendedDataMapperV2(currencyCodeMapper, MADRID_ZONE_ID),
                        clock,
                        TRANSACTION_DESCRIPTION_FALLBACK),
                BookingStatus.BOTH, new TransactionsFetchStartTimeNoLimited());
    }
}
