package com.yolt.providers.stet.cicgroup.cic.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.stet.cicgroup.cic.CicDataProviderV5;
import com.yolt.providers.stet.cicgroup.cic.service.fetchdata.CicFetchDataService;
import com.yolt.providers.stet.cicgroup.common.auth.CicGroupAuthenticationMeansSupplier;
import com.yolt.providers.stet.cicgroup.common.http.error.CicGroupHttpErrorHandler;
import com.yolt.providers.stet.cicgroup.common.http.signer.signature.CicGroupCavageSignatureStrategy;
import com.yolt.providers.stet.cicgroup.common.mapper.account.CicGroupAccountMapper;
import com.yolt.providers.stet.cicgroup.common.mapper.providerstate.CicGroupStateMapper;
import com.yolt.providers.stet.cicgroup.common.mapper.registration.CicGroupRegistrationRequestMapper;
import com.yolt.providers.stet.cicgroup.common.mapper.transaction.CicGroupTransactionMapper;
import com.yolt.providers.stet.cicgroup.common.service.authorization.rest.header.CicGroupAuthorizationNoSigningHttpHeadersFactory;
import com.yolt.providers.stet.cicgroup.common.service.authorization.tool.CicGroupCodeExchangeSupplier;
import com.yolt.providers.stet.cicgroup.common.service.fetchdata.rest.header.CicGroupFetchDataSigningHttpHeadersFactory;
import com.yolt.providers.stet.cicgroup.common.service.fetchdata.transaction.CicGroupFetchTransactionsStrategy;
import com.yolt.providers.stet.cicgroup.common.service.registration.CicGroupRegistrationService;
import com.yolt.providers.stet.cicgroup.common.service.registration.rest.CicGroupRegistrationRestClient;
import com.yolt.providers.stet.cicgroup.common.service.registration.rest.header.CicGroupGroupRegistrationHttpHeadersFactory;
import com.yolt.providers.stet.generic.auth.keyrequirements.DefaultKeyRequirementsProducer;
import com.yolt.providers.stet.generic.domain.DigestAlgorithm;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.http.client.DefaultHttpClientFactory;
import com.yolt.providers.stet.generic.http.signer.DefaultHttpSigner;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.token.DefaultTokenRequestMapper;
import com.yolt.providers.stet.generic.service.authorization.SingleRegionAuthorizationService;
import com.yolt.providers.stet.generic.service.authorization.refresh.RefreshTokenSupportedStrategy;
import com.yolt.providers.stet.generic.service.authorization.rest.DefaultAuthorizationRestClient;
import com.yolt.providers.stet.generic.service.authorization.tool.ConsentValidityRulesBuilder;
import com.yolt.providers.stet.generic.service.authorization.tool.DefaultAuthorizationCodeExtractor;
import com.yolt.providers.stet.generic.service.authorization.tool.DefaultPKCEAuthorizationRedirectUrlSupplier;
import com.yolt.providers.stet.generic.service.fetchdata.account.FetchBasicAccountsStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.balance.FetchBasicBalancesStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.rest.DefaultFetchDataRestClient;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Period;

@Configuration("CicStetBeanConfig")
public class CicBeanConfig {

    @Bean("CicDataProviderV5")
    public CicDataProviderV5 createCicDataProviderV5(@Qualifier("CicStetProperties") CicProperties properties,
                                                     MeterRegistry meterRegistry,
                                                     Clock clock,
                                                     @Qualifier("StetObjectMapper") ObjectMapper objectMapper) {
        CicGroupAuthenticationMeansSupplier authMeansSupplier = new CicGroupAuthenticationMeansSupplier(new DefaultKeyRequirementsProducer());
        CicGroupStateMapper providerStateMapper = new CicGroupStateMapper(objectMapper, properties);
        DefaultAuthorizationRestClient authorizationRestClient = new DefaultAuthorizationRestClient(
                new CicGroupAuthorizationNoSigningHttpHeadersFactory(),
                new DefaultTokenRequestMapper(),
                new CicGroupHttpErrorHandler());
        DefaultFetchDataRestClient fetchDataRestClient = new DefaultFetchDataRestClient(
                new CicGroupFetchDataSigningHttpHeadersFactory(
                        new DefaultHttpSigner(
                                new CicGroupCavageSignatureStrategy(SignatureAlgorithm.SHA256_WITH_RSA),
                                objectMapper,
                                DigestAlgorithm.SHA_256)),
                new CicGroupHttpErrorHandler());
        DateTimeSupplier cicGroupDateTimeSupplier = new DateTimeSupplier(clock);
        return new CicDataProviderV5(
                authMeansSupplier,
                new DefaultHttpClientFactory(meterRegistry, objectMapper),
                new CicGroupRegistrationService(
                        new CicGroupRegistrationRestClient(new CicGroupGroupRegistrationHttpHeadersFactory(),
                                new CicGroupRegistrationRequestMapper(),
                                properties),
                        authMeansSupplier,
                        properties),
                new SingleRegionAuthorizationService(
                        new RefreshTokenSupportedStrategy(
                                providerStateMapper,
                                Scope.AISP,
                                authorizationRestClient,
                                cicGroupDateTimeSupplier),
                        new DefaultAuthorizationRestClient(
                                new CicGroupAuthorizationNoSigningHttpHeadersFactory(),
                                new DefaultTokenRequestMapper()),
                        providerStateMapper,
                        Scope.AISP_EXTENDED_TRANSACTION_HISTORY,
                        properties,
                        new DefaultAuthorizationCodeExtractor(),
                        new DefaultPKCEAuthorizationRedirectUrlSupplier(new CicGroupCodeExchangeSupplier()),
                        cicGroupDateTimeSupplier),
                new CicFetchDataService(
                        fetchDataRestClient,
                        providerStateMapper,
                        new FetchBasicAccountsStrategy(fetchDataRestClient),
                        new CicGroupFetchTransactionsStrategy(
                                fetchDataRestClient,
                                cicGroupDateTimeSupplier,
                                properties,
                                Period.ofDays(89)),
                        new FetchBasicBalancesStrategy(fetchDataRestClient),
                        cicGroupDateTimeSupplier,
                        new CicGroupAccountMapper(cicGroupDateTimeSupplier),
                        new CicGroupTransactionMapper(cicGroupDateTimeSupplier),
                        properties),
                providerStateMapper,
                properties,
                ConsentValidityRulesBuilder.emptyRules()
        );
    }
}
