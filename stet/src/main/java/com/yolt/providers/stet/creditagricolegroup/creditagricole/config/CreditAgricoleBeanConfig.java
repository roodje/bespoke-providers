package com.yolt.providers.stet.creditagricolegroup.creditagricole.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.stet.creditagricolegroup.common.mapper.registration.CreditAgricoleGroupRegistrationRequestMapper;
import com.yolt.providers.stet.creditagricolegroup.common.service.authorization.rest.error.CreditAgricoleGroupAuthorizationHttpErrorHandler;
import com.yolt.providers.stet.creditagricolegroup.common.service.authorization.rest.header.CreditAgricoleGroupAuthorizationNoSigningHttpHeadersFactory;
import com.yolt.providers.stet.creditagricolegroup.common.service.fetchdata.CreditAgricoleGroupFetchDataService;
import com.yolt.providers.stet.creditagricolegroup.common.service.fetchdata.error.CreditAgricoleGroupFetchDataHttpErrorHandler;
import com.yolt.providers.stet.creditagricolegroup.common.service.fetchdata.rest.CreditAgricoleGroupFetchDataRestClient;
import com.yolt.providers.stet.creditagricolegroup.common.service.fetchdata.rest.header.CreditAgricoleGroupFetchDataHttpHeadersFactory;
import com.yolt.providers.stet.creditagricolegroup.common.service.fetchdata.transaction.CreditAgricoleGroupFetchTransactionsStrategy;
import com.yolt.providers.stet.creditagricolegroup.common.service.registration.CreditAgricoleGroupRegistrationService;
import com.yolt.providers.stet.creditagricolegroup.creditagricole.CreditAgricoleDataProviderV10;
import com.yolt.providers.stet.creditagricolegroup.creditagricole.auth.CreditAgricoleAuthenticationMeansSupplier;
import com.yolt.providers.stet.creditagricolegroup.creditagricole.mapper.providerstate.CreditAgricoleGroupProviderStateMapper;
import com.yolt.providers.stet.generic.auth.keyrequirements.DefaultKeyRequirementsProducer;
import com.yolt.providers.stet.generic.domain.DigestAlgorithm;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.http.client.DefaultHttpClientFactory;
import com.yolt.providers.stet.generic.http.signer.DefaultHttpSigner;
import com.yolt.providers.stet.generic.http.signer.signature.CavageSignatureStrategy;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.account.DefaultAccountMapper;
import com.yolt.providers.stet.generic.mapper.token.DefaultTokenRequestMapper;
import com.yolt.providers.stet.generic.mapper.transaction.DefaultTransactionMapper;
import com.yolt.providers.stet.generic.service.authorization.MultiRegionAuthorizationService;
import com.yolt.providers.stet.generic.service.authorization.refresh.RefreshTokenSupportedStrategy;
import com.yolt.providers.stet.generic.service.authorization.rest.DefaultAuthorizationRestClient;
import com.yolt.providers.stet.generic.service.authorization.tool.ConsentValidityRulesBuilder;
import com.yolt.providers.stet.generic.service.authorization.tool.DefaultAuthorizationCodeExtractor;
import com.yolt.providers.stet.generic.service.authorization.tool.DefaultAuthorizationRedirectUrlSupplier;
import com.yolt.providers.stet.generic.service.fetchdata.account.FetchDeclaredAccountsStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.balance.FetchExtractedBalancesStrategy;
import com.yolt.providers.stet.generic.service.registration.rest.DefaultRegistrationRestClient;
import com.yolt.providers.stet.generic.service.registration.rest.header.DefaultRegistrationHttpHeadersFactory;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration("CreditAgricoleStetBeanConfig")
public class CreditAgricoleBeanConfig {

    @Bean("CreditAgricoleDataProviderV10")
    public CreditAgricoleDataProviderV10 createCreditAgricoleDataProviderV10(MeterRegistry meterRegistry,
                                                                             Clock clock,
                                                                             @Qualifier("CreditAgricoleStetProperties") CreditAgricoleProperties properties,
                                                                             @Qualifier("StetObjectMapper") ObjectMapper objectMapper) {
        DefaultHttpSigner httpSigner = new DefaultHttpSigner(new CavageSignatureStrategy(SignatureAlgorithm.SHA256_WITH_RSA), objectMapper, DigestAlgorithm.SHA_256);
        CreditAgricoleAuthenticationMeansSupplier authMeansSupplier = new CreditAgricoleAuthenticationMeansSupplier(new DefaultKeyRequirementsProducer());
        CreditAgricoleGroupProviderStateMapper providerStateMapper = new CreditAgricoleGroupProviderStateMapper(objectMapper, properties);

        DefaultAuthorizationRestClient authorizationRestClient = new DefaultAuthorizationRestClient(
                new CreditAgricoleGroupAuthorizationNoSigningHttpHeadersFactory(),
                new DefaultTokenRequestMapper(),
                new CreditAgricoleGroupAuthorizationHttpErrorHandler());

        CreditAgricoleGroupFetchDataRestClient fetchDataRestClient = new CreditAgricoleGroupFetchDataRestClient(
                new CreditAgricoleGroupFetchDataHttpHeadersFactory(httpSigner, clock),
                new CreditAgricoleGroupFetchDataHttpErrorHandler());

        DateTimeSupplier dateTimeSupplier = new DateTimeSupplier(clock);
        return new CreditAgricoleDataProviderV10(
                authMeansSupplier,
                new DefaultHttpClientFactory(meterRegistry, objectMapper),
                new CreditAgricoleGroupRegistrationService(
                        new DefaultRegistrationRestClient(
                                new DefaultRegistrationHttpHeadersFactory(httpSigner),
                                new CreditAgricoleGroupRegistrationRequestMapper(),
                                properties),
                        authMeansSupplier,
                        properties),
                new MultiRegionAuthorizationService(
                        new RefreshTokenSupportedStrategy(
                                providerStateMapper,
                                Scope.AISP,
                                authorizationRestClient,
                                dateTimeSupplier),
                        authorizationRestClient,
                        providerStateMapper,
                        Scope.AISP_EXTENDED_TRANSACTION_HISTORY,
                        properties,
                        new DefaultAuthorizationCodeExtractor(),
                        new DefaultAuthorizationRedirectUrlSupplier(),
                        dateTimeSupplier),
                new CreditAgricoleGroupFetchDataService(
                        fetchDataRestClient,
                        providerStateMapper,
                        new FetchDeclaredAccountsStrategy(fetchDataRestClient),
                        new CreditAgricoleGroupFetchTransactionsStrategy(
                                fetchDataRestClient,
                                dateTimeSupplier,
                                properties),
                        new FetchExtractedBalancesStrategy(fetchDataRestClient),
                        dateTimeSupplier,
                        new DefaultAccountMapper(dateTimeSupplier),
                        new DefaultTransactionMapper(dateTimeSupplier),
                        properties),
                providerStateMapper,
                properties,
                ConsentValidityRulesBuilder.emptyRules());
    }
}
