package com.yolt.providers.cbiglobe.montepaschisiena;

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
public class MontePaschiSienaDataProviderV5 extends CbiGlobeDataProviderV5 {

    public MontePaschiSienaDataProviderV5(@Qualifier("MontePaschiSienaAuthenticationServiceV2") CbiGlobeAuthorizationService cbiGlobeAuthorizationService,
                                          @Qualifier("MontePaschiSienaConsentRequestServiceV4") CbiGlobeConsentRequestServiceV4 consentRequestService,
                                          @Qualifier("MontePaschiSienaHttpClientFactoryV2") CbiGlobeHttpClientFactory httpClientFactory,
                                          @Qualifier("MontePaschiSienaFetchServiceV4") CbiGlobeFetchDataServiceV3 fetchDataService,
                                          MontePaschiSienaProperties properties,
                                          @Qualifier("CbiGlobe") final ObjectMapper mapper,
                                          Clock clock) {
        super(cbiGlobeAuthorizationService, consentRequestService, httpClientFactory, fetchDataService, properties, mapper, clock);
    }

    @Override
    public String getProviderIdentifier() {
        return "MONTE_DEI_PASCHI_DI_SIENA";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Monte dei Paschi di Siena";
    }

    @Override
    public ProviderVersion getVersion() {
        return VERSION_5;
    }
}
