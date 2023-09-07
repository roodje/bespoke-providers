package com.yolt.providers.stet.bnpparibasgroup.hellobank;

import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.bnpparibasgroup.BnpParibasGroupDataProvider;
import com.yolt.providers.stet.generic.auth.ExtendedAuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.authorization.AuthorizationService;
import com.yolt.providers.stet.generic.service.fetchdata.FetchDataService;
import com.yolt.providers.stet.generic.service.registration.RegistrationService;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_6;

public class HelloBankDataProviderV6 extends BnpParibasGroupDataProvider {
    public HelloBankDataProviderV6(ExtendedAuthenticationMeansSupplier authMeansSupplier, HttpClientFactory httpClientFactory, RegistrationService registrationService, AuthorizationService authorizationService, FetchDataService fetchDataService, ProviderStateMapper providerStateMapper, DefaultProperties properties, ConsentValidityRules consentValidityRules) {
        super(authMeansSupplier, httpClientFactory, registrationService, authorizationService, fetchDataService, providerStateMapper, properties, consentValidityRules);
    }

    @Override
    public String getProviderIdentifier() {
        return "HELLO_BANK";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Hello bank!";
    }

    @Override
    public ProviderVersion getVersion() {
        return VERSION_6;
    }
}
