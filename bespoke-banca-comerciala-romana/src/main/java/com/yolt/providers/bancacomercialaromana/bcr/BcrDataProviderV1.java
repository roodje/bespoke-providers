package com.yolt.providers.bancacomercialaromana.bcr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bancacomercialaromana.common.BcrGroupDataProvider;
import com.yolt.providers.bancacomercialaromana.common.http.BcrGroupHttpClientFactory;
import com.yolt.providers.bancacomercialaromana.common.service.BcrGroupAuthorizationServiceV1;
import com.yolt.providers.bancacomercialaromana.common.service.BcrGroupFetchDataServiceV1;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;

import java.time.Clock;

public class BcrDataProviderV1 extends BcrGroupDataProvider {

    private static final String PROVIDER_IDENTIFIER = "BANCA_COMERCIALA_ROMANA";
    private static final String PROVIDER_DISPLAY_NAME = "Banca Comerciala Romana";

    public BcrDataProviderV1(ObjectMapper objectMapper,
                             BcrGroupAuthorizationServiceV1 authorizationService,
                             BcrGroupFetchDataServiceV1 fetchDataService,
                             BcrGroupHttpClientFactory httpClientFactory,
                             String s3BaseUrl,
                             Clock clock) {
        super(objectMapper, authorizationService, fetchDataService, httpClientFactory, s3BaseUrl, clock);
    }

    @Override
    public String getProviderIdentifier() {
        return PROVIDER_IDENTIFIER;
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return PROVIDER_DISPLAY_NAME;
    }

    @Override
    public ProviderVersion getVersion() {
        return ProviderVersion.VERSION_1;
    }

    @Override
    public ConsentValidityRules getConsentValidityRules() {
        return ConsentValidityRules.EMPTY_RULES_SET;
    }
}
