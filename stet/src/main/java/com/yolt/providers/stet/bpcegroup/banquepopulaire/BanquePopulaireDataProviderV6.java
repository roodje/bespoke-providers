package com.yolt.providers.stet.bpcegroup.banquepopulaire;

import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.bpcegroup.banquepopulaire.config.BanquePopulaireProperties;
import com.yolt.providers.stet.generic.GenericOnboardingDataProvider;
import com.yolt.providers.stet.generic.auth.ExtendedAuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.authorization.AuthorizationService;
import com.yolt.providers.stet.generic.service.fetchdata.FetchDataService;
import com.yolt.providers.stet.generic.service.registration.RegistrationService;

public class BanquePopulaireDataProviderV6 extends GenericOnboardingDataProvider {


    public BanquePopulaireDataProviderV6(ExtendedAuthenticationMeansSupplier authMeansSupplier,
                                         RegistrationService registrationService,
                                         HttpClientFactory httpClientFactory,
                                         AuthorizationService authorizationService,
                                         FetchDataService fetchDataService,
                                         ProviderStateMapper providerStateMapper,
                                         BanquePopulaireProperties banquePopulaireProperties,
                                         ConsentValidityRules consentValidityRules) {
        super(authMeansSupplier, httpClientFactory, registrationService, authorizationService, fetchDataService, providerStateMapper, banquePopulaireProperties, consentValidityRules);
    }

    @Override
    public String getProviderIdentifier() {
        return "BANQUE_POPULAIRE";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Banque Populaire";
    }

    @Override
    public ProviderVersion getVersion() {
        return ProviderVersion.VERSION_6;
    }
}
