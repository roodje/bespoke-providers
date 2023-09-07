package com.yolt.providers.stet.creditagricolegroup.creditagricole;

import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.generic.GenericOnboardingDataProvider;
import com.yolt.providers.stet.generic.auth.ExtendedAuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.authorization.AuthorizationService;
import com.yolt.providers.stet.generic.service.fetchdata.FetchDataService;
import com.yolt.providers.stet.generic.service.registration.RegistrationService;

public class CreditAgricoleDataProviderV10 extends GenericOnboardingDataProvider {

    public CreditAgricoleDataProviderV10(ExtendedAuthenticationMeansSupplier authMeansSupplier,
                                         HttpClientFactory httpClientFactory,
                                         RegistrationService registrationService,
                                         AuthorizationService authorizationService,
                                         FetchDataService fetchDataService,
                                         ProviderStateMapper providerStateMapper,
                                         DefaultProperties properties,
                                         ConsentValidityRules consentValidityRules) {
        super(authMeansSupplier, httpClientFactory, registrationService, authorizationService, fetchDataService, providerStateMapper, properties, consentValidityRules);
    }

    @Override
    public String getProviderIdentifier() {
        return "CREDITAGRICOLE";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Crédit Agricole";
    }

    @Override
    public ProviderVersion getVersion() {
        return ProviderVersion.VERSION_10;
    }
}
