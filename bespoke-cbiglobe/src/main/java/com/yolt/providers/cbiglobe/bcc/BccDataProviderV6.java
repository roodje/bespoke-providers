package com.yolt.providers.cbiglobe.bcc;

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

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_6;

@Service
public class BccDataProviderV6 extends CbiGlobeDataProviderV5 {

    public BccDataProviderV6(@Qualifier("BccAuthenticationServiceV2") CbiGlobeAuthorizationService authorizationService,
                             @Qualifier("BccConsentRequestServiceV4") CbiGlobeConsentRequestServiceV4 consentRequestService,
                             @Qualifier("BccHttpClientFactoryV2") CbiGlobeHttpClientFactory httpClientFactory,
                             @Qualifier("BccFetchServiceV4") CbiGlobeFetchDataServiceV3 fetchDataService,
                             BccProperties properties,
                             @Qualifier("CbiGlobe") final ObjectMapper mapper,
                             Clock clock) {
        super(authorizationService, consentRequestService, httpClientFactory, fetchDataService, properties, mapper, clock);
    }

    @Override
    public String getProviderIdentifier() {
        return "BCC_CREDITO_COOPERATIVO";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Banca Credito Cooperativo";
    }

    @Override
    public ProviderVersion getVersion() {
        return VERSION_6;
    }
}
