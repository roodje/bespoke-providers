package com.yolt.providers.redsys.cajarural;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.redsys.cajarural.consentretrieval.CajaRuralConsentProcessArgumentsMapper;
import com.yolt.providers.redsys.cajarural.consentretrieval.CajaRuralConsentRetrievalProcess;
import com.yolt.providers.redsys.cajarural.consentretrieval.CajaRuralSerializableConsentProcessData;
import com.yolt.providers.redsys.cajarural.service.mapper.CajaRuralExtendedDataMapper;
import com.yolt.providers.redsys.common.ProviderIdentification;
import com.yolt.providers.redsys.common.newgeneric.ResdysGenericStepDataProvider;
import com.yolt.providers.redsys.common.newgeneric.rest.RedsysHttpClientV2;
import com.yolt.providers.redsys.common.newgeneric.rest.RestTemplateService;
import com.yolt.providers.redsys.common.newgeneric.service.RedsysAuthorizationServiceV2;
import com.yolt.providers.redsys.common.newgeneric.service.RedsysFetchDataServiceV4;
import com.yolt.providers.redsys.common.rest.BookingStatus;
import com.yolt.providers.redsys.common.service.RedsysAllAccountsConsentObjectService;
import com.yolt.providers.redsys.common.service.TransactionsFetchStartTimeNoLimited;
import com.yolt.providers.redsys.common.service.mapper.CurrencyCodeMapper;
import com.yolt.providers.redsys.common.service.mapper.CurrencyCodeMapperV1;
import com.yolt.providers.redsys.common.service.mapper.RedsysDataMapperServiceV3;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.ZoneId;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;
import static com.yolt.providers.redsys.cajarural.CajaRuralDetailsProvider.CAJA_RURAL_PROVIDER_DISPLAY_NAME;
import static com.yolt.providers.redsys.cajarural.CajaRuralDetailsProvider.CAJA_RURAL_PROVIDER_KEY;

@Service
public class CajaRuralBeanConfig {

    private static final ZoneId MADRID_ZONE_ID = ZoneId.of("Europe/Madrid");
    private static final String TRANSACTION_DESCRIPTION_FALLBACK = "Description not available";

    @Bean("CajaRuralDataProviderV1")
    public ResdysGenericStepDataProvider<CajaRuralSerializableConsentProcessData> getCajaRuralDataProviderV1(final CajaRuralProperties properties,
                                                                                                             @Qualifier("Redsys") final ObjectMapper mapper,
                                                                                                             final Clock clock) {
        CurrencyCodeMapper currencyCodeMapper = new CurrencyCodeMapperV1();
        RestTemplateService restTemplateService = new RestTemplateService(mapper, properties);
        RedsysAuthorizationServiceV2<CajaRuralSerializableConsentProcessData> authorizationService = new RedsysAuthorizationServiceV2<>(
                restTemplateService,
                new RedsysAllAccountsConsentObjectService(),
                properties,
                new RedsysHttpClientV2());
        RedsysFetchDataServiceV4 fetchDataService = new RedsysFetchDataServiceV4(
                restTemplateService,
                properties,
                new RedsysDataMapperServiceV3(
                        currencyCodeMapper,
                        new CajaRuralExtendedDataMapper(currencyCodeMapper, MADRID_ZONE_ID),
                        clock,
                        TRANSACTION_DESCRIPTION_FALLBACK),
                BookingStatus.BOTH,
                new TransactionsFetchStartTimeNoLimited(),
                new RedsysHttpClientV2());
        CajaRuralConsentProcessArgumentsMapper cajaRuralConsentProcessArgumentsMapper = new CajaRuralConsentProcessArgumentsMapper(mapper, CAJA_RURAL_PROVIDER_KEY);
        return new ResdysGenericStepDataProvider<>(
                authorizationService,
                fetchDataService,
                new ProviderIdentification(CAJA_RURAL_PROVIDER_KEY, CAJA_RURAL_PROVIDER_DISPLAY_NAME, VERSION_1),
                new CajaRuralConsentRetrievalProcess(cajaRuralConsentProcessArgumentsMapper, authorizationService, properties, clock),
                cajaRuralConsentProcessArgumentsMapper,
                clock
        );
    }
}
