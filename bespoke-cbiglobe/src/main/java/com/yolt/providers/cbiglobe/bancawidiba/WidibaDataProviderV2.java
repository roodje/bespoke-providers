package com.yolt.providers.cbiglobe.bancawidiba;

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
public class WidibaDataProviderV2 extends CbiGlobeDataProviderV5 {

    public WidibaDataProviderV2(@Qualifier("WidibaAuthenticationServiceV2") CbiGlobeAuthorizationService cbiGlobeAuthorizationService,
                                @Qualifier("WidibaConsentRequestServiceV2") CbiGlobeConsentRequestServiceV4 consentRequestService,
                                @Qualifier("WidibaHttpClientFactoryV2") CbiGlobeHttpClientFactory httpClientFactory,
                                @Qualifier("WidibaFetchServiceV2") CbiGlobeFetchDataServiceV3 fetchDataService,
                                WidibaProperties properties,
                                @Qualifier("CbiGlobe") final ObjectMapper mapper,
                                Clock clock) {
        super(cbiGlobeAuthorizationService, consentRequestService, httpClientFactory, fetchDataService, properties, mapper, clock);
    }

    @Override
    public String getProviderIdentifier() {
        return "BANCA_WIDIBA";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Banca Widiba";
    }

    @Override
    public ProviderVersion getVersion() {
        return VERSION_2;
    }
}
