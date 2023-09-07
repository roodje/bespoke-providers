package com.yolt.providers.stet.bnpparibasgroup.hellobank.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.stet.bnpparibasgroup.common.auth.BnpParibasGroupAuthenticationMeansSupplier;
import com.yolt.providers.stet.bnpparibasgroup.common.http.BnpParibasGroupErrorHandler;
import com.yolt.providers.stet.bnpparibasgroup.common.http.BnpParibasGroupSignatureStrategy;
import com.yolt.providers.stet.bnpparibasgroup.common.mapper.BnpParibasGroupDataTimeSupplier;
import com.yolt.providers.stet.bnpparibasgroup.common.mapper.BnpParibasGroupProviderStateMapper;
import com.yolt.providers.stet.bnpparibasgroup.common.mapper.fetchdata.BnpParibasGroupBalanceMapper;
import com.yolt.providers.stet.bnpparibasgroup.common.mapper.fetchdata.BnpParibasGroupTransactionMapper;
import com.yolt.providers.stet.bnpparibasgroup.common.mapper.registration.BnpParibasGroupRegistrationRequestMapper;
import com.yolt.providers.stet.bnpparibasgroup.common.mapper.registration.BnpParibasRegistrationHttpHeadersFactory;
import com.yolt.providers.stet.bnpparibasgroup.common.mapper.token.BnpParibasGroupTokenRequestMapper;
import com.yolt.providers.stet.bnpparibasgroup.common.service.fetchdata.BnpParibasGroupFetchDataService;
import com.yolt.providers.stet.bnpparibasgroup.common.service.fetchdata.BnpParibasGroupFetchTransactionsStrategy;
import com.yolt.providers.stet.bnpparibasgroup.common.service.registration.BnpParibasGroupRegistrationService;
import com.yolt.providers.stet.bnpparibasgroup.common.service.registration.rest.BnpParibasGroupRegistrationRestClient;
import com.yolt.providers.stet.bnpparibasgroup.common.service.rest.BnpParibasGroupAuthorizationHttpHeadersFactory;
import com.yolt.providers.stet.bnpparibasgroup.hellobank.HelloBankDataProviderV6;
import com.yolt.providers.stet.bnpparibasgroup.hellobank.mapper.HelloBankAccountMapper;
import com.yolt.providers.stet.generic.auth.keyrequirements.DefaultKeyRequirementsProducer;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.DigestAlgorithm;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.http.client.DefaultHttpClientFactory;
import com.yolt.providers.stet.generic.http.signer.DefaultHttpSigner;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.service.authorization.SingleRegionAuthorizationService;
import com.yolt.providers.stet.generic.service.authorization.refresh.RefreshTokenSupportedStrategy;
import com.yolt.providers.stet.generic.service.authorization.rest.DefaultAuthorizationRestClient;
import com.yolt.providers.stet.generic.service.authorization.tool.ConsentValidityRulesBuilder;
import com.yolt.providers.stet.generic.service.authorization.tool.DefaultAuthorizationCodeExtractor;
import com.yolt.providers.stet.generic.service.authorization.tool.DefaultAuthorizationRedirectUrlSupplier;
import com.yolt.providers.stet.generic.service.fetchdata.account.FetchBasicAccountsStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.balance.FetchBasicBalancesStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.rest.DefaultFetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.rest.header.FetchDataSigningHttpHeadersFactory;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Period;
import java.time.ZoneId;

@Configuration("HelloBankStetBeanConfig")
//TODO name for bean should be removed in C4PO-9738
public class HelloBankBeanConfig {

    private static final Period FETCH_PERIOD = Period.ofDays(89);

    @Bean("HelloBankDataProviderV6")
    public HelloBankDataProviderV6 getHelloBankDataProviderV6(MeterRegistry meterRegistry,
                                                              Clock clock,
                                                              @Qualifier("HelloBankStetProperties") HelloBankProperties properties,
                                                              @Qualifier("StetObjectMapper") ObjectMapper objectMapper) {
        HttpSigner httpSigner = createHttpSigner(objectMapper);
        BnpParibasGroupErrorHandler errorHandler = new BnpParibasGroupErrorHandler();
        DefaultAuthorizationRestClient authorizationRestClient = new DefaultAuthorizationRestClient(
                new BnpParibasGroupAuthorizationHttpHeadersFactory(),
                new BnpParibasGroupTokenRequestMapper(),
                errorHandler
        );

        BnpParibasGroupAuthenticationMeansSupplier authMeansSupplier = new BnpParibasGroupAuthenticationMeansSupplier(properties, new DefaultKeyRequirementsProducer());
        DateTimeSupplier dateTimeSupplier = new BnpParibasGroupDataTimeSupplier(clock, ZoneId.of("Europe/Paris"));
        BnpParibasGroupRegistrationRestClient registrationRestClient = getRegistrationRestClient(httpSigner, properties, objectMapper);
        BnpParibasGroupProviderStateMapper providerStateMapper = new BnpParibasGroupProviderStateMapper(objectMapper, properties);
        FetchDataSigningHttpHeadersFactory headersFactory = new FetchDataSigningHttpHeadersFactory(httpSigner);
        DefaultFetchDataRestClient fetchDataRestClient = new DefaultFetchDataRestClient(headersFactory,
                errorHandler);

        return new HelloBankDataProviderV6(authMeansSupplier,
                new DefaultHttpClientFactory(meterRegistry, objectMapper),
                new BnpParibasGroupRegistrationService(registrationRestClient, authMeansSupplier, properties),
                new SingleRegionAuthorizationService(
                        new RefreshTokenSupportedStrategy(
                                providerStateMapper,
                                Scope.AISP,
                                authorizationRestClient,
                                dateTimeSupplier),
                        authorizationRestClient,
                        providerStateMapper,
                        Scope.AISP,
                        properties,
                        new DefaultAuthorizationCodeExtractor(),
                        new DefaultAuthorizationRedirectUrlSupplier(),
                        dateTimeSupplier
                ),
                new BnpParibasGroupFetchDataService(
                        fetchDataRestClient,
                        providerStateMapper,
                        new FetchBasicAccountsStrategy(fetchDataRestClient),
                        new BnpParibasGroupFetchTransactionsStrategy(fetchDataRestClient, dateTimeSupplier, properties, FETCH_PERIOD, clock),
                        new FetchBasicBalancesStrategy(fetchDataRestClient),
                        dateTimeSupplier,
                        new HelloBankAccountMapper(dateTimeSupplier, new BnpParibasGroupBalanceMapper()),
                        new BnpParibasGroupTransactionMapper(dateTimeSupplier),
                        properties
                ),
                providerStateMapper,
                properties,
                ConsentValidityRulesBuilder.consentPageRules()
                        .containsKeyword("Me connecter")
                        .containsKeyword("Mon numéro client")
                        .containsKeyword("Mon code secret (6 chiffres)")
                        .build()
        );
    }

    private HttpSigner createHttpSigner(ObjectMapper objectMapper) {
        return new DefaultHttpSigner(new BnpParibasGroupSignatureStrategy(SignatureAlgorithm.SHA256_WITH_RSA),
                objectMapper,
                DigestAlgorithm.SHA_256);
    }

    private BnpParibasGroupRegistrationRestClient getRegistrationRestClient(HttpSigner signer, DefaultProperties properties, ObjectMapper objectMapper) {
        return new BnpParibasGroupRegistrationRestClient(
                new BnpParibasRegistrationHttpHeadersFactory(signer),
                new BnpParibasGroupRegistrationRequestMapper(),
                properties);
    }
}
