package com.yolt.providers.stet.cmarkeagroup.common;

import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.generic.GenericDataProvider;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.authorization.AuthorizationService;
import com.yolt.providers.stet.generic.service.fetchdata.FetchDataService;

public class CmArkeaGroupDataProvider extends GenericDataProvider {

    private final String providerIdentifier;
    private final String providerDisplayName;
    private final ProviderVersion version;

    public CmArkeaGroupDataProvider(AuthenticationMeansSupplier authMeansSupplier,
                                    HttpClientFactory httpClientFactory,
                                    AuthorizationService authorizationService,
                                    FetchDataService fetchDataService,
                                    ProviderStateMapper providerStateMapper,
                                    ConsentValidityRules consentValidityRules,
                                    String providerIdentifier,
                                    String providerDisplayName,
                                    ProviderVersion version) {
        super(authMeansSupplier, httpClientFactory, authorizationService, fetchDataService, providerStateMapper, consentValidityRules);
        this.providerIdentifier = providerIdentifier;
        this.providerDisplayName = providerDisplayName;
        this.version = version;
    }

    @Override
    public String getProviderIdentifier() {
        return providerIdentifier;
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return providerDisplayName;
    }

    @Override
    public ProviderVersion getVersion() {
        return version;
    }
}
