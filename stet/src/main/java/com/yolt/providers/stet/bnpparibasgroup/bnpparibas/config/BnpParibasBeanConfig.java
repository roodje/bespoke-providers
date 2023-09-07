package com.yolt.providers.stet.bnpparibasgroup.bnpparibas.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.bnpparibasgroup.bnpparibas.BnpParibasDataProviderV6;
import com.yolt.providers.stet.bnpparibasgroup.common.auth.BnpParibasGroupAuthenticationMeansSupplier;
import com.yolt.providers.stet.bnpparibasgroup.common.http.BnpParibasGroupErrorHandler;
import com.yolt.providers.stet.bnpparibasgroup.common.http.BnpParibasGroupSignatureStrategy;
import com.yolt.providers.stet.bnpparibasgroup.common.mapper.BnpParibasGroupDataTimeSupplier;
import com.yolt.providers.stet.bnpparibasgroup.common.mapper.BnpParibasGroupProviderStateMapper;
import com.yolt.providers.stet.bnpparibasgroup.common.mapper.fetchdata.BnpParibasGroupAccountMapper;
import com.yolt.providers.stet.bnpparibasgroup.common.mapper.fetchdata.BnpParibasGroupBalanceMapper;
import com.yolt.providers.stet.bnpparibasgroup.common.mapper.fetchdata.BnpParibasGroupTransactionMapper;
import com.yolt.providers.stet.bnpparibasgroup.common.mapper.registration.BnpParibasGroupRegistrationRequestMapper;
import com.yolt.providers.stet.bnpparibasgroup.common.mapper.registration.BnpParibasRegistrationHttpHeadersFactory;
import com.yolt.providers.stet.bnpparibasgroup.common.mapper.token.BnpParibasGroupTokenRequestMapper;
import com.yolt.providers.stet.bnpparibasgroup.common.pec.BnpParibasGroupGenericPaymentProviderFactory;
import com.yolt.providers.stet.bnpparibasgroup.common.pec.BnpParibasGroupPaymentHttpClientFactory;
import com.yolt.providers.stet.bnpparibasgroup.common.service.fetchdata.BnpParibasGroupFetchDataService;
import com.yolt.providers.stet.bnpparibasgroup.common.service.fetchdata.BnpParibasGroupFetchTransactionsStrategy;
import com.yolt.providers.stet.bnpparibasgroup.common.service.registration.BnpParibasGroupRegistrationService;
import com.yolt.providers.stet.bnpparibasgroup.common.service.registration.rest.BnpParibasGroupRegistrationRestClient;
import com.yolt.providers.stet.bnpparibasgroup.common.service.rest.BnpParibasGroupAuthorizationHttpHeadersFactory;
import com.yolt.providers.stet.generic.GenericPaymentProviderV3;
import com.yolt.providers.stet.generic.auth.keyrequirements.DefaultKeyRequirementsProducer;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.DigestAlgorithm;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.http.client.DefaultHttpClientFactory;
import com.yolt.providers.stet.generic.http.error.NoActionHttpErrorHandler;
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
import com.yolt.providers.stet.generic.service.pec.authorization.StetPaymentAuthorizationUrlExtractor;
import com.yolt.providers.stet.generic.service.pec.common.StetSigningPaymentHttpHeadersFactory;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Period;
import java.time.ZoneId;

@Configuration("BnpParibasStetBeanConfig")
public class BnpParibasBeanConfig {

    private static final Period FETCH_PERIOD = Period.ofDays(89);

    @Bean("BnpParibasDataProviderV6")
    public BnpParibasDataProviderV6 getBnpParibasDataProviderV6(MeterRegistry meterRegistry,
                                                                Clock clock,
                                                                @Qualifier("BnpParibasStetProperties") BnpParibasProperties properties,
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

        return new BnpParibasDataProviderV6(authMeansSupplier,
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
                        new BnpParibasGroupAccountMapper(dateTimeSupplier, new BnpParibasGroupBalanceMapper()),
                        new BnpParibasGroupTransactionMapper(dateTimeSupplier),
                        properties
                ),
                providerStateMapper,
                properties,
                ConsentValidityRulesBuilder.consentPageRules()
                        .containsKeyword("Access to my accounts online | BNP Paribas")
                        .build()
        );
    }

    @Bean("BnpParibasPaymentProviderV2")
    public GenericPaymentProviderV3 bnpParibasPaymentProviderV2(MeterRegistry meterRegistry,
                                                                BnpParibasProperties properties,
                                                                Clock clock,
                                                                @Qualifier("StetObjectMapper") ObjectMapper objectMapper) {
        ObjectMapper copyOfObjectMapper = objectMapper.copy();
        return new BnpParibasGroupGenericPaymentProviderFactory(
                new ProviderIdentification(
                        "BNP_PARIBAS",
                        "BNP Paribas",
                        ProviderVersion.VERSION_2),
                new StetPaymentAuthorizationUrlExtractor(),
                new StetSigningPaymentHttpHeadersFactory(createHttpSigner(copyOfObjectMapper),
                        ExternalTracingUtil::createLastExternalTraceId),
                new BnpParibasGroupPaymentHttpClientFactory(meterRegistry, copyOfObjectMapper),
                new NoActionHttpErrorHandler(),
                new BnpParibasGroupAuthenticationMeansSupplier(properties,
                        new DefaultKeyRequirementsProducer()),
                new DateTimeSupplier(clock),
                new BnpParibasGroupProviderStateMapper(copyOfObjectMapper, properties),
                properties,
                ConsentValidityRulesBuilder.consentPageRules()
                        .containsKeyword("Customer number")
                        .containsKeyword("PIN Code (6 digits)")
                        .containsKeyword("Submit")
                        .build(),
                Scope.PISP,
                copyOfObjectMapper,
                clock).createPaymentProvider();
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
