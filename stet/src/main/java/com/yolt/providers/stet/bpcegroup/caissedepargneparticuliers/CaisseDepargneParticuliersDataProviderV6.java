package com.yolt.providers.stet.bpcegroup.caissedepargneparticuliers;

import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.bpcegroup.caissedepargneparticuliers.config.CaisseDepargneParticuliersProperties;
import com.yolt.providers.stet.generic.GenericOnboardingDataProvider;
import com.yolt.providers.stet.generic.auth.ExtendedAuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.authorization.AuthorizationService;
import com.yolt.providers.stet.generic.service.fetchdata.FetchDataService;
import com.yolt.providers.stet.generic.service.registration.RegistrationService;

public class CaisseDepargneParticuliersDataProviderV6 extends GenericOnboardingDataProvider {

    public CaisseDepargneParticuliersDataProviderV6(ExtendedAuthenticationMeansSupplier authMeansSupplier,
                                                    RegistrationService registrationService,
                                                    HttpClientFactory httpClientFactory,
                                                    AuthorizationService authorizationService,
                                                    FetchDataService fetchDataService,
                                                    ProviderStateMapper providerStateMapper,
                                                    CaisseDepargneParticuliersProperties properties,
                                                    ConsentValidityRules consentValidityRules) {
        super(authMeansSupplier, httpClientFactory, registrationService, authorizationService, fetchDataService, providerStateMapper, properties, consentValidityRules);
    }

    @Override
    public String getProviderIdentifier() {
        return "CAISSE_DEPARGNE_PARTICULIERS";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Caisse d'epargne particuliers";
    }

    @Override
    public ProviderVersion getVersion() {
        return ProviderVersion.VERSION_6;
    }
}
