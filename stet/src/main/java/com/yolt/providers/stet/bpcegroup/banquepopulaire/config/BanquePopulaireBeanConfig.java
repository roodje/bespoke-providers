package com.yolt.providers.stet.bpcegroup.banquepopulaire.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.bpcegroup.banquepopulaire.BanquePopulaireDataProviderV6;
import com.yolt.providers.stet.bpcegroup.common.auth.BpceAuthenticationMeansSupplier;
import com.yolt.providers.stet.bpcegroup.common.http.BpceGroupSignatureStrategy;
import com.yolt.providers.stet.bpcegroup.common.http.error.BpceGroupAuthorizationErrorHandler;
import com.yolt.providers.stet.bpcegroup.common.mapper.account.BpceGroupAccountMapper;
import com.yolt.providers.stet.bpcegroup.common.mapper.providerstate.BpceProviderStateMapper;
import com.yolt.providers.stet.bpcegroup.common.mapper.token.BpceGroupTokenRequestMapper;
import com.yolt.providers.stet.bpcegroup.common.mapper.transaction.BpceGroupTransactionMapper;
import com.yolt.providers.stet.bpcegroup.common.onboarding.BpceGroupRegistrationHttpHeadersFactory;
import com.yolt.providers.stet.bpcegroup.common.onboarding.BpceGroupRegistrationRequestMapper;
import com.yolt.providers.stet.bpcegroup.common.onboarding.BpceGroupRegistrationRestClient;
import com.yolt.providers.stet.bpcegroup.common.onboarding.BpceGroupRegistrationService;
import com.yolt.providers.stet.bpcegroup.common.service.authorization.BpceGroupMultiRegionAuthorizationService;
import com.yolt.providers.stet.bpcegroup.common.service.fetchdata.BpceGroupFetchDataService;
import com.yolt.providers.stet.bpcegroup.common.service.fetchdata.BpceGroupFetchTransactionsStrategy;
import com.yolt.providers.stet.bpcegroup.common.service.fetchdata.account.BpceGroupFetchDeclaredAccountsStrategy;
import com.yolt.providers.stet.bpcegroup.common.service.fetchdata.rest.BpceGroupFetchDataSigningHttpHeadersFactory;
import com.yolt.providers.stet.bpcegroup.common.service.fetchdata.rest.error.BpceGroupFetchDataHttpErrorHandler;
import com.yolt.providers.stet.bpcegroup.common.service.pec.BpceGroupGenericPaymentProviderV1Factory;
import com.yolt.providers.stet.bpcegroup.common.service.pec.BpceSigningPaymentHttpHeadersFactory;
import com.yolt.providers.stet.generic.GenericPaymentProviderV3;
import com.yolt.providers.stet.generic.auth.keyrequirements.DefaultKeyRequirementsProducer;
import com.yolt.providers.stet.generic.domain.DigestAlgorithm;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.http.client.DefaultHttpClientFactory;
import com.yolt.providers.stet.generic.http.client.DefaultHttpClientFactoryV2;
import com.yolt.providers.stet.generic.http.error.NoActionHttpErrorHandlerV2;
import com.yolt.providers.stet.generic.http.signer.DefaultHttpSigner;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.service.authorization.refresh.RefreshTokenSupportedStrategy;
import com.yolt.providers.stet.generic.service.authorization.rest.DefaultAuthorizationRestClient;
import com.yolt.providers.stet.generic.service.authorization.rest.header.DefaultAuthorizationNoSigningHttpHeadersFactory;
import com.yolt.providers.stet.generic.service.authorization.tool.ConsentValidityRulesBuilder;
import com.yolt.providers.stet.generic.service.authorization.tool.DefaultAuthorizationCodeExtractor;
import com.yolt.providers.stet.generic.service.authorization.tool.DefaultAuthorizationRedirectUrlSupplier;
import com.yolt.providers.stet.generic.service.fetchdata.balance.FetchExtractedBalancesStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.rest.DefaultFetchDataRestClient;
import com.yolt.providers.stet.generic.service.pec.authorization.StetPaymentAuthorizationUrlExtractor;
import com.yolt.providers.stet.generic.service.registration.DefaultRegistrationService;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Period;

@Configuration("BanquePopulaireStetBeanConfig")
public class BanquePopulaireBeanConfig {

    @Bean("BanquePopulaireDataProviderV6")
    public BanquePopulaireDataProviderV6 createBanquePopulaireDataProviderV6(MeterRegistry meterRegistry,
                                                                             Clock clock,
                                                                             @Qualifier("BanquePopulaireStetProperties") BanquePopulaireProperties properties,
                                                                             @Qualifier("StetObjectMapper") ObjectMapper objectMapper) {
        DefaultHttpSigner httpSigner = new DefaultHttpSigner(new BpceGroupSignatureStrategy(SignatureAlgorithm.SHA256_WITH_RSA), objectMapper, DigestAlgorithm.SHA_256);
        BpceAuthenticationMeansSupplier authMeansSupplier = new BpceAuthenticationMeansSupplier(new DefaultKeyRequirementsProducer());
        BpceProviderStateMapper providerStateMapper = new BpceProviderStateMapper(objectMapper, properties);
        DefaultRegistrationService bpceGroupAutoOnBoardingService = new BpceGroupRegistrationService(new BpceGroupRegistrationRestClient(new BpceGroupRegistrationHttpHeadersFactory(httpSigner, properties), new BpceGroupRegistrationRequestMapper(), properties), authMeansSupplier);

        DefaultAuthorizationRestClient authorizationRestClient = new DefaultAuthorizationRestClient(
                new DefaultAuthorizationNoSigningHttpHeadersFactory(),
                new BpceGroupTokenRequestMapper(),
                new BpceGroupAuthorizationErrorHandler());

        DefaultFetchDataRestClient fetchDataRestClient = new DefaultFetchDataRestClient(
                new BpceGroupFetchDataSigningHttpHeadersFactory(httpSigner, clock),
                new BpceGroupFetchDataHttpErrorHandler());

        DateTimeSupplier dateTimeSupplier = new DateTimeSupplier(clock);
        return new BanquePopulaireDataProviderV6(
                authMeansSupplier,
                bpceGroupAutoOnBoardingService,
                new DefaultHttpClientFactory(meterRegistry, objectMapper),
                new BpceGroupMultiRegionAuthorizationService(
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
                        dateTimeSupplier),
                new BpceGroupFetchDataService(
                        fetchDataRestClient,
                        providerStateMapper,
                        new BpceGroupFetchDeclaredAccountsStrategy(fetchDataRestClient),
                        new BpceGroupFetchTransactionsStrategy(
                                fetchDataRestClient,
                                dateTimeSupplier,
                                properties,
                                Period.ofDays(89)),
                        new FetchExtractedBalancesStrategy(fetchDataRestClient),
                        dateTimeSupplier,
                        new BpceGroupAccountMapper(dateTimeSupplier),
                        new BpceGroupTransactionMapper(dateTimeSupplier),
                        properties),
                providerStateMapper,
                properties,
                ConsentValidityRulesBuilder.emptyRules());
    }

    @Bean("BanquePopulairePaymentProviderV2")
    public GenericPaymentProviderV3 createBanquePopulairePaymentProviderV2(MeterRegistry meterRegistry,
                                                                           Clock clock,
                                                                           @Qualifier("BanquePopulaireStetProperties") BanquePopulaireProperties properties,
                                                                           @Qualifier("StetObjectMapper") ObjectMapper objectMapper) {
        ObjectMapper copyOfObjectMapper = objectMapper.copy();
        DefaultHttpSigner httpSigner = new DefaultHttpSigner(new BpceGroupSignatureStrategy(SignatureAlgorithm.SHA256_WITH_RSA), copyOfObjectMapper, DigestAlgorithm.SHA_256);

        return new BpceGroupGenericPaymentProviderV1Factory(
                new ProviderIdentification(
                        "BANQUE_POPULAIRE",
                        "Banque Populaire",
                        ProviderVersion.VERSION_2),
                new StetPaymentAuthorizationUrlExtractor(),
                new BpceSigningPaymentHttpHeadersFactory(httpSigner, clock),
                new DefaultHttpClientFactoryV2(meterRegistry, copyOfObjectMapper),
                new NoActionHttpErrorHandlerV2(),
                new BpceAuthenticationMeansSupplier(new DefaultKeyRequirementsProducer()),
                new DateTimeSupplier(clock),
                new BpceProviderStateMapper(copyOfObjectMapper, properties),
                properties,
                ConsentValidityRulesBuilder.emptyRules(),
                copyOfObjectMapper,
                clock)
                .createPaymentProvider();
    }
}
