package com.yolt.providers.stet.boursoramagroup.boursorama;

import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.generic.GenericDataProvider;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.authorization.AuthorizationService;
import com.yolt.providers.stet.generic.service.authorization.tool.ConsentValidityRulesBuilder;
import com.yolt.providers.stet.generic.service.fetchdata.FetchDataService;

public class BoursoramaDataProviderV4 extends GenericDataProvider {

    public BoursoramaDataProviderV4(AuthenticationMeansSupplier authMeansSupplier,
                                    HttpClientFactory httpClientFactory,
                                    AuthorizationService authorizationService,
                                    FetchDataService fetchDataService,
                                    ProviderStateMapper providerStateMapper) {
        super(authMeansSupplier,
                httpClientFactory,
                authorizationService,
                fetchDataService,
                providerStateMapper,
                ConsentValidityRulesBuilder.consentPageRules()
                        .containsKeyword("Mon identifiant")
                        .containsKeyword("Saisissez votre identifiant")
                        .containsKeyword("Suivant")
                        .build());
    }

    @Override
    public String getProviderIdentifier() {
        return "BOURSORAMA";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Boursorama";
    }

    @Override
    public ProviderVersion getVersion() {
        return ProviderVersion.VERSION_4;
    }
}
