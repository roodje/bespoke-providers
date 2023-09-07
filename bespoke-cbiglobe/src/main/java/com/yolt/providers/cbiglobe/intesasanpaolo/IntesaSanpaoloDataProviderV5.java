package com.yolt.providers.cbiglobe.intesasanpaolo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.common.CbiGlobeDataProviderV5;
import com.yolt.providers.cbiglobe.common.service.CbiGlobeAuthorizationService;
import com.yolt.providers.cbiglobe.common.service.CbiGlobeConsentRequestServiceV4;
import com.yolt.providers.cbiglobe.common.service.CbiGlobeFetchDataServiceV3;
import com.yolt.providers.cbiglobe.common.service.CbiGlobeHttpClientFactory;
import com.yolt.providers.common.versioning.ProviderVersion;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Clock;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_5;

@Service
public class IntesaSanpaoloDataProviderV5 extends CbiGlobeDataProviderV5 {

    public IntesaSanpaoloDataProviderV5(@Qualifier("IntesaSanpaoloAuthenticationServiceV2") CbiGlobeAuthorizationService authorizationService,
                                        @Qualifier("IntesaSanpaoloConsentRequestServiceV4") CbiGlobeConsentRequestServiceV4 consentRequestService,
                                        @Qualifier("IntesaSanpaoloHttpClientFactoryV2") CbiGlobeHttpClientFactory httpClientFactory,
                                        @Qualifier("IntesaSanpaoloFetchServiceV4") CbiGlobeFetchDataServiceV3 fetchDataService,
                                        IntesaSanpaoloProperties properties,
                                        @Qualifier("CbiGlobe") final ObjectMapper mapper,
                                        Clock clock) {
        super(authorizationService, consentRequestService, httpClientFactory, fetchDataService, properties, mapper, clock);
    }

    @Override
    public String getProviderIdentifier() {
        return "INTESA_SANPAOLO";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Intesa Sanpaolo";
    }

    @Override
    public ProviderVersion getVersion() {
        return VERSION_5;
    }
}
