package com.yolt.providers.stet.cmarkeagroup.fortuneo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.cmarkeagroup.common.CmArkeaGroupDataProvider;
import com.yolt.providers.stet.cmarkeagroup.common.auth.CmArkeaGroupAuthenticationMeansSupplier;
import com.yolt.providers.stet.cmarkeagroup.common.http.CmArkeaGroupSignatureStrategy;
import com.yolt.providers.stet.cmarkeagroup.common.http.error.CmArkeaGroupErrorHandler;
import com.yolt.providers.stet.cmarkeagroup.common.mapper.CmArkeaGroupDateTimeSupplier;
import com.yolt.providers.stet.cmarkeagroup.common.mapper.CmArkeaGroupObjectMapperFactory;
import com.yolt.providers.stet.cmarkeagroup.common.mapper.account.CmArkeaGroupAccountMapper;
import com.yolt.providers.stet.cmarkeagroup.common.mapper.providerstate.CmArkeaGroupProviderStateMapper;
import com.yolt.providers.stet.cmarkeagroup.common.service.fetchdata.CmArkeaFetchTransactionsStrategyV2;
import com.yolt.providers.stet.cmarkeagroup.common.service.fetchdata.CmArkeaGroupFetchDataService;
import com.yolt.providers.stet.cmarkeagroup.common.service.rest.CmArkeaGroupAuthorizationHttpHeadersFactory;
import com.yolt.providers.stet.cmarkeagroup.common.service.rest.header.CmArkeaFetchDataSigningHttpHeadersFactory;
import com.yolt.providers.stet.generic.auth.keyrequirements.DefaultKeyRequirementsProducer;
import com.yolt.providers.stet.generic.domain.DigestAlgorithm;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.http.client.DefaultHttpClientFactory;
import com.yolt.providers.stet.generic.http.signer.DefaultHttpSigner;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import com.yolt.providers.stet.generic.mapper.token.DefaultTokenRequestMapper;
import com.yolt.providers.stet.generic.mapper.transaction.DefaultTransactionMapper;
import com.yolt.providers.stet.generic.service.authorization.SingleRegionAuthorizationService;
import com.yolt.providers.stet.generic.service.authorization.refresh.RefreshTokenSupportedStrategy;
import com.yolt.providers.stet.generic.service.authorization.rest.DefaultAuthorizationRestClient;
import com.yolt.providers.stet.generic.service.authorization.tool.ConsentValidityRulesBuilder;
import com.yolt.providers.stet.generic.service.authorization.tool.DefaultAuthorizationCodeExtractor;
import com.yolt.providers.stet.generic.service.authorization.tool.DefaultAuthorizationRedirectUrlSupplier;
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
import java.time.ZoneId;

@Configuration("FortuneoStetBeanConfig")
public class FortuneoBeanConfig {

    private static final Period FETCH_PERIOD = Period.ofDays(89);
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Paris");

    @Bean("FortuneoDataProviderV3")
    public CmArkeaGroupDataProvider createFortuneoDataProviderV3(MeterRegistry meterRegistry,
                                                                 Clock clock,
                                                                 @Qualifier("FortuneoStetProperties") FortuneoProperties properties,
                                                                 @Qualifier("StetObjectMapper") ObjectMapper stetObjectMapper) {
        CmArkeaGroupObjectMapperFactory objectMapperFactory = new CmArkeaGroupObjectMapperFactory(stetObjectMapper, ZONE_ID);
        ObjectMapper objectMapper = objectMapperFactory.getObjectMapper();

        CmArkeaGroupAuthenticationMeansSupplier authMeansSupplier = new CmArkeaGroupAuthenticationMeansSupplier(new DefaultKeyRequirementsProducer(), properties);
        HttpSigner httpSigner = createHttpSigner(objectMapper);

        DefaultAuthorizationRestClient authorizationRestClient = new DefaultAuthorizationRestClient(
                new CmArkeaGroupAuthorizationHttpHeadersFactory(objectMapper, httpSigner),
                new DefaultTokenRequestMapper(),
                new CmArkeaGroupErrorHandler()
        );

        CmArkeaGroupProviderStateMapper providerStateMapper = new CmArkeaGroupProviderStateMapper(objectMapper, properties);
        DefaultFetchDataRestClient fetchDataRestClient = new DefaultFetchDataRestClient(
                new CmArkeaFetchDataSigningHttpHeadersFactory(
                        new DefaultHttpSigner(
                                new CmArkeaGroupSignatureStrategy(SignatureAlgorithm.SHA256_WITH_RSA),
                                objectMapper,
                                DigestAlgorithm.SHA_256), "1.4.2"),
                new CmArkeaGroupErrorHandler());
        CmArkeaGroupDateTimeSupplier dateTimeSupplier = new CmArkeaGroupDateTimeSupplier(clock, ZONE_ID);
        return new CmArkeaGroupDataProvider(
                authMeansSupplier,
                new DefaultHttpClientFactory(meterRegistry, objectMapper),
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
                        dateTimeSupplier),
                new CmArkeaGroupFetchDataService(
                        fetchDataRestClient,
                        providerStateMapper,
                        new FetchBasicAccountsStrategy(fetchDataRestClient),
                        new CmArkeaFetchTransactionsStrategyV2(
                                fetchDataRestClient,
                                dateTimeSupplier,
                                properties,
                                FETCH_PERIOD),
                        new FetchBasicBalancesStrategy(fetchDataRestClient),
                        dateTimeSupplier,
                        new CmArkeaGroupAccountMapper(dateTimeSupplier),
                        new DefaultTransactionMapper(dateTimeSupplier),
                        properties
                ),
                providerStateMapper,
                ConsentValidityRulesBuilder.emptyRules(),
                "FORTUNEO",
                "Fortuneo Banque",
                ProviderVersion.VERSION_3
        );
    }

    private HttpSigner createHttpSigner(ObjectMapper objectMapper) {
        CmArkeaGroupSignatureStrategy signatureStrategy = new CmArkeaGroupSignatureStrategy(SignatureAlgorithm.SHA256_WITH_RSA);
        return new DefaultHttpSigner(signatureStrategy, objectMapper, DigestAlgorithm.SHA_256);
    }
}
