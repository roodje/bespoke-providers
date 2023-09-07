package com.yolt.providers.triodosbank.nl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.triodosbank.common.TriodosBankDataProvider;
import com.yolt.providers.triodosbank.common.rest.TriodosBankHttpClientFactory;
import com.yolt.providers.triodosbank.common.service.TriodosBankAuthorizationService;
import com.yolt.providers.triodosbank.common.service.TriodosBankFetchDataService;
import com.yolt.providers.triodosbank.common.service.TriodosBankRegistrationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class TriodosBankNLDataProvider extends TriodosBankDataProvider {

    public TriodosBankNLDataProvider(@Qualifier("TriodosBankNLRegistrationService") TriodosBankRegistrationService registrationService,
                                     @Qualifier("TriodosBankNLAuthorizationService") TriodosBankAuthorizationService authorizationService,
                                     @Qualifier("TriodosBankNLFetchDataService") TriodosBankFetchDataService fetchDataService,
                                     @Qualifier("TriodosBankNLHttpClientFactory") TriodosBankHttpClientFactory httpClientFactory,
                                     @Qualifier("TriodosBankObjectMapper") ObjectMapper objectMapper,
                                     Clock clock) {
        super(registrationService, authorizationService, fetchDataService, httpClientFactory, objectMapper, clock);
    }

    @Override
    public String getProviderIdentifier() {
        return "TRIODOS_BANK_NL";
    }

    @Override
    public String getProviderIdentifierDisplayName() {
        return "Triodos Bank (NL)";
    }

    @Override
    public ProviderVersion getVersion() {
        return ProviderVersion.VERSION_1;
    }
}
