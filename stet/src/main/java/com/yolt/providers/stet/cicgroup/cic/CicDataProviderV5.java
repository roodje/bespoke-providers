package com.yolt.providers.stet.cicgroup.cic;

import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.cicgroup.common.CicGroupAbstractDataProvider;
import com.yolt.providers.stet.cicgroup.common.service.registration.CicGroupRegistrationService;
import com.yolt.providers.stet.generic.auth.ExtendedAuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.authorization.AuthorizationService;
import com.yolt.providers.stet.generic.service.fetchdata.FetchDataService;

public class CicDataProviderV5 extends CicGroupAbstractDataProvider {

    public CicDataProviderV5(ExtendedAuthenticationMeansSupplier authMeansSupplier,
                             HttpClientFactory httpClientFactory,
                             CicGroupRegistrationService registrationService,
                             AuthorizationService authorizationService,
                             FetchDataService fetchDataService,
                             ProviderStateMapper providerStateMapper,
                             DefaultProperties properties,
                             ConsentValidityRules consentValidityRules) {
        super(authMeansSupplier, httpClientFactory, registrationService, authorizationService, fetchDataService, providerStateMapper, properties, consentValidityRules);
    }

    @Override
    public String getProviderIdentifier() {
        return "CIC";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Crédit Industriel et Commercial";
    }

    @Override
    public ProviderVersion getVersion() {
        return ProviderVersion.VERSION_5;
    }
}
