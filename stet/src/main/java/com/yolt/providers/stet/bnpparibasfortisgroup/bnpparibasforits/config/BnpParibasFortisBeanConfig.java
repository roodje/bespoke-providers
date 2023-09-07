package com.yolt.providers.stet.bnpparibasfortisgroup.bnpparibasforits.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.stet.bnpparibasfortisgroup.bnpparibasforits.BnpParibasFortisDataProviderV2;
import com.yolt.providers.stet.bnpparibasfortisgroup.bnpparibasforits.auth.BnpParibasFortisAuthenticationMeansSupplier;
import com.yolt.providers.stet.bnpparibasfortisgroup.bnpparibasforits.mapper.providerstate.BnpParibasFortisProviderStateMapper;
import com.yolt.providers.stet.bnpparibasfortisgroup.common.mapper.account.BnpParibasFortisGroupAccountMapper;
import com.yolt.providers.stet.bnpparibasfortisgroup.common.mapper.registration.BnpParibasFortisGroupRegistrationRequestMapper;
import com.yolt.providers.stet.bnpparibasfortisgroup.common.mapper.token.BnpParibasFortisGroupTokenRequestMapper;
import com.yolt.providers.stet.bnpparibasfortisgroup.common.mapper.transaction.BnpParibasFortisGroupTransactionMapper;
import com.yolt.providers.stet.bnpparibasfortisgroup.common.service.authorization.rest.header.BnpParibasFortisGroupAuthorizationHttpHeadersFactory;
import com.yolt.providers.stet.bnpparibasfortisgroup.common.service.fetchdata.BnpParibasFortisGroupFetchDataService;
import com.yolt.providers.stet.bnpparibasfortisgroup.common.service.fetchdata.BnpParibasFortisGroupFetchTransactionsStrategy;
import com.yolt.providers.stet.bnpparibasfortisgroup.common.service.registration.rest.header.BnpParibasFortisGroupRegistrationHttpHeadersFactory;
import com.yolt.providers.stet.generic.auth.keyrequirements.DefaultKeyRequirementsProducer;
import com.yolt.providers.stet.generic.domain.DigestAlgorithm;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.http.client.DefaultHttpClientFactory;
import com.yolt.providers.stet.generic.http.signer.DefaultHttpSigner;
import com.yolt.providers.stet.generic.http.signer.signature.EnhancedCavageSignatureStrategy;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.service.authorization.SingleRegionAuthorizationService;
import com.yolt.providers.stet.generic.service.authorization.refresh.RefreshTokenUnsupportedStrategy;
import com.yolt.providers.stet.generic.service.authorization.rest.DefaultAuthorizationRestClient;
import com.yolt.providers.stet.generic.service.authorization.tool.ConsentValidityRulesBuilder;
import com.yolt.providers.stet.generic.service.authorization.tool.DefaultAuthorizationCodeExtractor;
import com.yolt.providers.stet.generic.service.authorization.tool.DefaultAuthorizationRedirectUrlSupplier;
import com.yolt.providers.stet.generic.service.fetchdata.account.FetchBasicAccountsStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.balance.FetchBasicBalancesStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.rest.DefaultFetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.rest.header.FetchDataSigningHttpHeadersFactory;
import com.yolt.providers.stet.generic.service.registration.DefaultRegistrationService;
import com.yolt.providers.stet.generic.service.registration.rest.DefaultRegistrationRestClient;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Period;
import java.time.ZoneId;

@Configuration("BnpParibasFortisStetBeanConfig")
public class BnpParibasFortisBeanConfig {

    @Bean("BnpParibasFortisDataProviderV2")
    public BnpParibasFortisDataProviderV2 createBnpParibasFortisDataProviderV2(@Qualifier("BnpParibasFortisStetProperties") BnpParibasFortisProperties properties,
                                                                               MeterRegistry meterRegistry,
                                                                               Clock clock,
                                                                               @Qualifier("StetObjectMapper") ObjectMapper objectMapper) {
        BnpParibasFortisAuthenticationMeansSupplier authMeansSupplier = new BnpParibasFortisAuthenticationMeansSupplier(new DefaultKeyRequirementsProducer(), properties);
        BnpParibasFortisProviderStateMapper providerStateMapper = new BnpParibasFortisProviderStateMapper(objectMapper, properties);
        DefaultFetchDataRestClient fetchDataRestClient = new DefaultFetchDataRestClient(
                new FetchDataSigningHttpHeadersFactory(
                        new DefaultHttpSigner(
                                new EnhancedCavageSignatureStrategy(SignatureAlgorithm.SHA256_WITH_RSA),
                                objectMapper,
                                DigestAlgorithm.SHA_256)));
        DateTimeSupplier dateTimeSupplier = new DateTimeSupplier(clock, ZoneId.of("Europe/Brussels"));
        return new BnpParibasFortisDataProviderV2(
                authMeansSupplier,
                new DefaultHttpClientFactory(meterRegistry, objectMapper),
                new DefaultRegistrationService(
                        new DefaultRegistrationRestClient(new BnpParibasFortisGroupRegistrationHttpHeadersFactory(objectMapper),
                                new BnpParibasFortisGroupRegistrationRequestMapper(),
                                properties),
                        authMeansSupplier),
                new SingleRegionAuthorizationService(
                        new RefreshTokenUnsupportedStrategy(),
                        new DefaultAuthorizationRestClient(
                                new BnpParibasFortisGroupAuthorizationHttpHeadersFactory(),
                                new BnpParibasFortisGroupTokenRequestMapper()),
                        providerStateMapper,
                        Scope.AISP,
                        properties,
                        new DefaultAuthorizationCodeExtractor(),
                        new DefaultAuthorizationRedirectUrlSupplier(),
                        dateTimeSupplier),
                new BnpParibasFortisGroupFetchDataService(
                        fetchDataRestClient,
                        providerStateMapper,
                        new FetchBasicAccountsStrategy(fetchDataRestClient),
                        new BnpParibasFortisGroupFetchTransactionsStrategy(
                                fetchDataRestClient,
                                dateTimeSupplier,
                                properties,
                                Period.ofDays(179)),
                        new FetchBasicBalancesStrategy(fetchDataRestClient),
                        dateTimeSupplier,
                        new BnpParibasFortisGroupAccountMapper(dateTimeSupplier),
                        new BnpParibasFortisGroupTransactionMapper(dateTimeSupplier),
                        properties
                ),
                providerStateMapper,
                properties,
                ConsentValidityRulesBuilder.emptyRules()
        );
    }
}
