package com.yolt.providers.cbiglobe.bnl;

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
public class BnlDataProviderV5 extends CbiGlobeDataProviderV5 {

    public BnlDataProviderV5(@Qualifier("BnlAuthenticationServiceV2") CbiGlobeAuthorizationService authorizationService,
                             @Qualifier("BnlConsentRequestServiceV4") CbiGlobeConsentRequestServiceV4 consentRequestService,
                             @Qualifier("BnlHttpClientFactoryV2") CbiGlobeHttpClientFactory httpClientFactory,
                             @Qualifier("BnlFetchServiceV4") CbiGlobeFetchDataServiceV3 fetchDataService,
                             BnlProperties properties,
                             @Qualifier("CbiGlobe") final ObjectMapper mapper,
                             Clock clock) {
        super(authorizationService, consentRequestService, httpClientFactory, fetchDataService, properties, mapper, clock);
    }

    @Override
    public String getProviderIdentifier() {
        return "BANCA_NAZIONALE_DEL_LAVORO";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Banca Nazionale del Lavoro";
    }

    @Override
    public ProviderVersion getVersion() {
        return VERSION_5;
    }
}
