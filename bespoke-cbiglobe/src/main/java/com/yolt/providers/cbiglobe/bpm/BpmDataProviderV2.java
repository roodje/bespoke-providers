package com.yolt.providers.cbiglobe.bpm;

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

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_2;

@Service
public class BpmDataProviderV2 extends CbiGlobeDataProviderV5 {

    public BpmDataProviderV2(@Qualifier("BpmAuthenticationServiceV2") CbiGlobeAuthorizationService authorizationService,
                             @Qualifier("BpmConsentRequestServiceV4") CbiGlobeConsentRequestServiceV4 consentRequestService,
                             @Qualifier("BpmHttpClientFactoryV2") CbiGlobeHttpClientFactory httpClientFactory,
                             @Qualifier("BpmFetchServiceV4") CbiGlobeFetchDataServiceV3 fetchDataService,
                             BpmProperties properties,
                             @Qualifier("CbiGlobe") final ObjectMapper mapper,
                             Clock clock) {
        super(authorizationService, consentRequestService, httpClientFactory, fetchDataService, properties, mapper, clock);
    }

    @Override
    public String getProviderIdentifier() {
        return "BANCO_BPM";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Banco BPM";
    }

    @Override
    public ProviderVersion getVersion() {
        return VERSION_2;
    }
}
