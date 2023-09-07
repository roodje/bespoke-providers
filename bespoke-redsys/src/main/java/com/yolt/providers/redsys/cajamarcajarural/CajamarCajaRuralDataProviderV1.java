package com.yolt.providers.redsys.cajamarcajarural;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.redsys.common.RedsysDataProviderV2;
import com.yolt.providers.redsys.common.service.RedsysAuthorizationService;
import com.yolt.providers.redsys.common.service.RedsysFetchDataServiceV2;

import java.time.Clock;
import java.util.Set;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;
import static com.yolt.providers.redsys.cajamarcajarural.CajamarCajaRuralDetailsProvider.CAJAMAR_CAJA_RURAL_PROVIDER_DISPLAY_NAME;
import static com.yolt.providers.redsys.cajamarcajarural.CajamarCajaRuralDetailsProvider.CAJAMAR_CAJA_RURAL_PROVIDER_KEY;

public class CajamarCajaRuralDataProviderV1 extends RedsysDataProviderV2 {

    public CajamarCajaRuralDataProviderV1(final CajamarCajaRuralProperties properties,
                                          final RedsysAuthorizationService authorizationService,
                                          final RedsysFetchDataServiceV2 fetchDataService,
                                          final ObjectMapper mapper,
                                          Clock clock) {
        super(properties, authorizationService, fetchDataService, mapper, clock);
    }

    @Override
    public String getProviderIdentifier() {
        return CAJAMAR_CAJA_RURAL_PROVIDER_KEY;
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return CAJAMAR_CAJA_RURAL_PROVIDER_DISPLAY_NAME;
    }

    @Override
    public ProviderVersion getVersion() {
        return VERSION_1;
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return new ConsentValidityRules(Set.of("Open Banking Grupo Cajamar"));
    }
}
