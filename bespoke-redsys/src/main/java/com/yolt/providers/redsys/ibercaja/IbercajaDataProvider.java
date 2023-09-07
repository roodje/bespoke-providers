package com.yolt.providers.redsys.ibercaja;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.redsys.common.RedsysDataProviderV2;
import com.yolt.providers.redsys.common.service.RedsysAuthorizationService;
import com.yolt.providers.redsys.common.service.RedsysFetchDataServiceV3;

import java.time.Clock;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;

public class IbercajaDataProvider extends RedsysDataProviderV2 {

    public static final String PROVIDER_IDENTIFIER = "IBERCAJA";
    public static final String PROVIDER_IDENTIFIER_NAME = "Ibercaja";

    public IbercajaDataProvider(final IbercajaProperties properties,
                                final RedsysAuthorizationService authorizationService,
                                final RedsysFetchDataServiceV3 fetchDataService,
                                final ObjectMapper mapper,
                                Clock clock) {
        super(properties, authorizationService, fetchDataService, mapper, clock);
    }

    @Override
    public String getProviderIdentifier() {
        return PROVIDER_IDENTIFIER;
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return PROVIDER_IDENTIFIER_NAME;
    }

    @Override
    public ProviderVersion getVersion() {
        return VERSION_1;
    }
}
