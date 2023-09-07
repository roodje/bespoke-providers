package com.yolt.providers.cbiglobe.posteitaliane;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.cbiglobe.common.CbiGlobeDataProviderV5;
import com.yolt.providers.cbiglobe.common.service.CbiGlobeAuthorizationService;
import com.yolt.providers.cbiglobe.common.service.CbiGlobeConsentRequestServiceV4;
import com.yolt.providers.cbiglobe.common.service.CbiGlobeFetchDataServiceV3;
import com.yolt.providers.cbiglobe.common.service.CbiGlobeHttpClientFactory;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.HashSet;
import java.util.Set;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_5;

@Service
public class PosteItalianeDataProviderV5 extends CbiGlobeDataProviderV5 {

    public PosteItalianeDataProviderV5(@Qualifier("PosteItalianeAuthenticationServiceV2") CbiGlobeAuthorizationService authorizationService,
                                       @Qualifier("PosteItalianeConsentRequestServiceV4") CbiGlobeConsentRequestServiceV4 consentRequestService,
                                       @Qualifier("PosteItalianeHttpClientFactoryV2") CbiGlobeHttpClientFactory httpClientFactory,
                                       @Qualifier("PosteItalianeFetchServiceV4") CbiGlobeFetchDataServiceV3 fetchDataService,
                                       PosteItalianeProperties properties,
                                       @Qualifier("CbiGlobe") final ObjectMapper mapper,
                                       Clock clock) {
        super(authorizationService, consentRequestService, httpClientFactory, fetchDataService, properties, mapper, clock);
    }

    @Override
    public String getProviderIdentifier() {
        return "POSTE_ITALIANE";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Poste Italiane";
    }

    @Override
    public ProviderVersion getVersion() {
        return VERSION_5;
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        Set<String> keywords = new HashSet<>();
        keywords.add("Accedi");
        keywords.add("Per accedere a");
        keywords.add("Inserisci le tue");
        keywords.add("credenziali");
        return new ConsentValidityRules(keywords);
    }
}
