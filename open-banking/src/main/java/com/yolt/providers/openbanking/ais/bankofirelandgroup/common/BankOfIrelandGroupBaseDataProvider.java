package com.yolt.providers.openbanking.ais.bankofirelandgroup.common;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansMapper;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;


public class BankOfIrelandGroupBaseDataProvider extends GenericBaseDataProvider {

    public BankOfIrelandGroupBaseDataProvider(FetchDataService fetchDataService,
                                              AccountRequestService accountRequestService,
                                              AuthenticationService authenticationService,
                                              HttpClientFactory httpClientFactory,
                                              TokenScope scope,
                                              ProviderIdentification providerIdentification,
                                              Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans,
                                              Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier,
                                              AccessMeansMapper accessMeansMapper, Supplier<Optional<KeyRequirements>> getSigningKeyRequirements,
                                              Supplier<Optional<KeyRequirements>> getTransportKeyRequirements,
                                              Supplier<ConsentValidityRules> consentValidityRulesSupplier) {
        super(fetchDataService, accountRequestService, authenticationService, httpClientFactory, scope, providerIdentification,
                getAuthenticationMeans, typedAuthenticationMeansSupplier, accessMeansMapper, getSigningKeyRequirements,
                getTransportKeyRequirements, consentValidityRulesSupplier);
    }
}
