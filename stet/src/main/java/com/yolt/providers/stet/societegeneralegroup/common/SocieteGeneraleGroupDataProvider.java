package com.yolt.providers.stet.societegeneralegroup.common;

import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.generic.GenericDataProvider;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.authorization.AuthorizationService;
import com.yolt.providers.stet.generic.service.fetchdata.FetchDataService;

public class SocieteGeneraleGroupDataProvider extends GenericDataProvider {

    private final ProviderIdentification providerIdentification;

    public SocieteGeneraleGroupDataProvider(ProviderIdentification providerIdentification,
                                            AuthenticationMeansSupplier authMeansSupplier,
                                            HttpClientFactory httpClientFactory,
                                            AuthorizationService authorizationService,
                                            FetchDataService fetchDataService,
                                            ProviderStateMapper providerStateMapper,
                                            ConsentValidityRules consentValidityRules) {
        super(authMeansSupplier, httpClientFactory, authorizationService, fetchDataService, providerStateMapper, consentValidityRules);
        this.providerIdentification = providerIdentification;
    }

    @Override
    public String getProviderIdentifier() {
        return providerIdentification.getIdentifier();
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return providerIdentification.getDisplayName();
    }

    @Override
    public ProviderVersion getVersion() {
        return providerIdentification.getVersion();
    }
}
