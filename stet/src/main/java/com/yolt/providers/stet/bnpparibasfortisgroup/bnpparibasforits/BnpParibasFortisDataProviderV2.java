package com.yolt.providers.stet.bnpparibasfortisgroup.bnpparibasforits;

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

public class BnpParibasFortisDataProviderV2 extends GenericOnboardingDataProvider {

    public BnpParibasFortisDataProviderV2(ExtendedAuthenticationMeansSupplier authMeansSupplier,
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
        return "BNP_PARIBAS_FORTIS";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "BNP Paribas Fortis";
    }

    @Override
    public ProviderVersion getVersion() {
        return ProviderVersion.VERSION_2;
    }
}
