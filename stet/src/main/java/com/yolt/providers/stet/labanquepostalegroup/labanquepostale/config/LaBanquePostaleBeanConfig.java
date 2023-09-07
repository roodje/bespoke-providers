package com.yolt.providers.stet.labanquepostalegroup.labanquepostale.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.generic.GenericPaymentProviderV3;
import com.yolt.providers.stet.generic.auth.keyrequirements.DefaultKeyRequirementsProducer;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.http.client.DefaultHttpClientFactoryV2;
import com.yolt.providers.stet.generic.http.error.NoActionHttpErrorHandlerV2;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.transaction.DefaultTransactionMapper;
import com.yolt.providers.stet.generic.service.authorization.SingleRegionAuthorizationService;
import com.yolt.providers.stet.generic.service.authorization.refresh.RefreshTokenUnsupportedStrategy;
import com.yolt.providers.stet.generic.service.authorization.rest.DefaultAuthorizationRestClient;
import com.yolt.providers.stet.generic.service.authorization.tool.ConsentValidityRulesBuilder;
import com.yolt.providers.stet.generic.service.fetchdata.DefaultFetchDataService;
import com.yolt.providers.stet.generic.service.fetchdata.account.FetchBasicAccountsStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.balance.FetchBasicBalancesStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.rest.header.FetchDataNoSigningHttpHeadersFactory;
import com.yolt.providers.stet.generic.service.fetchdata.transaction.DefaultFetchTransactionsStrategy;
import com.yolt.providers.stet.generic.service.pec.authorization.StetPaymentAuthorizationUrlExtractor;
import com.yolt.providers.stet.generic.service.pec.common.StetNoSigningPaymentHttpHeadersFactory;
import com.yolt.providers.stet.generic.service.registration.DefaultRegistrationService;
import com.yolt.providers.stet.generic.service.registration.rest.DefaultRegistrationRestClient;
import com.yolt.providers.stet.labanquepostalegroup.common.http.LaBanquePostaleHttpClientFactory;
import com.yolt.providers.stet.labanquepostalegroup.common.mapper.account.LaBanquePostaleGroupAccountMapper;
import com.yolt.providers.stet.labanquepostalegroup.common.mapper.registration.LaBanquePostaleGroupRegistrationRequestMapper;
import com.yolt.providers.stet.labanquepostalegroup.common.mapper.token.LaBanquePostaleGroupTokenRequestMapper;
import com.yolt.providers.stet.labanquepostalegroup.common.service.authorization.rest.header.LaBanquePostaleGroupAuthorizationNoSigningHttpHeadersFactory;
import com.yolt.providers.stet.labanquepostalegroup.common.service.authorization.tool.LaBanquePostaleGroupAuthorizationCodeExtractor;
import com.yolt.providers.stet.labanquepostalegroup.common.service.authorization.tool.LaBanquePostaleRedirectUrlSupplier;
import com.yolt.providers.stet.labanquepostalegroup.common.service.fetchdata.rest.LaBanquePostaleGroupFetchDataRestClient;
import com.yolt.providers.stet.labanquepostalegroup.common.service.pec.LaBanquePostaleGroupGenericPaymentProviderV2Factory;
import com.yolt.providers.stet.labanquepostalegroup.common.service.registration.rest.header.LaBanquePostaleGroupRegistrationHttpHeadersFactory;
import com.yolt.providers.stet.labanquepostalegroup.labanquepostale.LaBanquePostaleDataProviderV5;
import com.yolt.providers.stet.labanquepostalegroup.labanquepostale.auth.LaBanquePostaleAuthenticationMeansSupplier;
import com.yolt.providers.stet.labanquepostalegroup.labanquepostale.mapper.providerstate.LaBanquePostaleProviderStateMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Period;

@Configuration("LaBanquePostaleStetBeanConfig")
public class LaBanquePostaleBeanConfig {

    private static final Period FETCH_PERIOD = Period.ofDays(89);
    private static final String PROVIDER_IDENTIFIER = "LA_BANQUE_POSTALE";
    private static final String PROVIDER_DISPLAY_NAME = "La Banque Postale";

    @Bean("LaBanquePostaleDataProviderV5")
    public LaBanquePostaleDataProviderV5 createLaBanquePostaleDataProviderV5(MeterRegistry meterRegistry,
                                                                             Clock clock,
                                                                             @Qualifier("LaBanquePostaleStetProperties") LaBanquePostaleProperties properties,
                                                                             @Qualifier("StetObjectMapper") ObjectMapper objectMapper) {
        LaBanquePostaleAuthenticationMeansSupplier authMeansSupplier = new LaBanquePostaleAuthenticationMeansSupplier(new DefaultKeyRequirementsProducer());
        LaBanquePostaleGroupFetchDataRestClient fetchDataRestClient = new LaBanquePostaleGroupFetchDataRestClient(new FetchDataNoSigningHttpHeadersFactory());
        DateTimeSupplier dateTimeSupplier = new DateTimeSupplier(clock);
        LaBanquePostaleProviderStateMapper providerStateMapper = new LaBanquePostaleProviderStateMapper(objectMapper, properties);

        return new LaBanquePostaleDataProviderV5(
                authMeansSupplier,
                new LaBanquePostaleHttpClientFactory(meterRegistry, objectMapper),
                new DefaultRegistrationService(new DefaultRegistrationRestClient(
                        new LaBanquePostaleGroupRegistrationHttpHeadersFactory(),
                        new LaBanquePostaleGroupRegistrationRequestMapper(),
                        properties),
                        authMeansSupplier),
                new SingleRegionAuthorizationService(
                        new RefreshTokenUnsupportedStrategy(),
                        new DefaultAuthorizationRestClient(
                                new LaBanquePostaleGroupAuthorizationNoSigningHttpHeadersFactory(),
                                new LaBanquePostaleGroupTokenRequestMapper()),
                        providerStateMapper,
                        Scope.AISP,
                        properties,
                        new LaBanquePostaleGroupAuthorizationCodeExtractor(),
                        new LaBanquePostaleRedirectUrlSupplier(),
                        dateTimeSupplier),
                new DefaultFetchDataService(
                        fetchDataRestClient,
                        providerStateMapper,
                        new FetchBasicAccountsStrategy(fetchDataRestClient),
                        new DefaultFetchTransactionsStrategy(fetchDataRestClient, dateTimeSupplier, properties, FETCH_PERIOD),
                        new FetchBasicBalancesStrategy(fetchDataRestClient),
                        dateTimeSupplier,
                        new LaBanquePostaleGroupAccountMapper(dateTimeSupplier),
                        new DefaultTransactionMapper(dateTimeSupplier),
                        properties),
                providerStateMapper,
                properties,
                ConsentValidityRulesBuilder.consentPageRules()
                        .containsKeyword("Identification - La Banque Postale")
                        .containsKeyword("mot de passe")
                        .build());
    }

    @Bean("LaBanquePostalePaymentProviderV2")
    public GenericPaymentProviderV3 createLaBanquePostalePaymentProviderV2(MeterRegistry meterRegistry,
                                                                           Clock clock,
                                                                           @Qualifier("LaBanquePostaleStetProperties") LaBanquePostaleProperties properties,
                                                                           @Qualifier("StetObjectMapper") ObjectMapper objectMapper) {
        ObjectMapper laBanquePostelObjectMapper = objectMapper.copy();
        return new LaBanquePostaleGroupGenericPaymentProviderV2Factory(
                new ProviderIdentification(
                        PROVIDER_IDENTIFIER,
                        PROVIDER_DISPLAY_NAME,
                        ProviderVersion.VERSION_2),
                new StetPaymentAuthorizationUrlExtractor(),
                new StetNoSigningPaymentHttpHeadersFactory(),
                new DefaultHttpClientFactoryV2(meterRegistry, laBanquePostelObjectMapper),
                new NoActionHttpErrorHandlerV2(),
                new LaBanquePostaleAuthenticationMeansSupplier(new DefaultKeyRequirementsProducer()),
                new DateTimeSupplier(clock),
                new LaBanquePostaleProviderStateMapper(laBanquePostelObjectMapper, properties),
                properties,
                ConsentValidityRulesBuilder.consentPageRules()
                        .containsKeyword("Saisissez ici votre identifiant")
                        .containsKeyword("Composez votre mot de passe")
                        .containsKeyword("Valider")
                        .build(),
                laBanquePostelObjectMapper,
                clock)
                .createPaymentProvider();
    }
}
