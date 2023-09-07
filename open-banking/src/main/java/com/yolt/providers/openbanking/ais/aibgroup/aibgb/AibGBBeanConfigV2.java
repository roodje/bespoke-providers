package com.yolt.providers.openbanking.ais.aibgroup.aibgb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.openbanking.ais.aibgroup.common.auth.AibGroupAuthMeansBuilderV3;
import com.yolt.providers.openbanking.ais.aibgroup.common.http.AibGroupHttpClientFactoryV2;
import com.yolt.providers.openbanking.ais.aibgroup.common.oauth2.AibGroupOAuth2ClientV2;
import com.yolt.providers.openbanking.ais.aibgroup.common.pec.AibGroupPaymentRequestValidator;
import com.yolt.providers.openbanking.ais.aibgroup.common.service.ais.accountmapper.AibGroupAccountMapperV3;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV3;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.FapiCompliantJwtClaimsProducerDecorator;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.consentvalidity.DefaultConsentValidityRulesSupplier;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.pec.DefaultPaymentExecutionContextAdapterFactoryV2;
import com.yolt.providers.openbanking.ais.generic2.pec.common.UkProviderStateDeserializer;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.amount.DefaultAmountFormatter;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.datainitiation.PaymentDataInitiationMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.instructionidentification.DateTimeAndUUIDBasedInstructionIdentificationSupplier;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme.CamelCaseUkSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericConsentResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericScheduledConsentResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericScheduledResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericStatusResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.NoCacheAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultConsentWindow;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.supportedaccounts.DefaultSupportedAccountsSupplier;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountNumberMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountIdMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountNameMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.schemesupport.DefaultSupportedSchemeAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper.DefaultAccountReferenceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.amount.DefaultAmountParser;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.DefaultBalanceMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balanceamount.DefaultBalanceAmountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balancetype.DefaultBalanceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.creditcard.CreditCardMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.currency.DefaultCurrencyMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedaccount.DefaultExtendedAccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedbalance.DefaultExtendedBalancesMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedtransaction.DefaultExtendedTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultAccessMeansMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.period.DefaultPeriodMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme.DefaultSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.time.DefaultDateTimeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultDirectDebitMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultStandingOrderMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.DefaultTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.PendingAsNullTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactiontype.DefaultTransactionTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentNoB64RequestSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentRequestSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBRisk1;
import io.micrometer.core.instrument.MeterRegistry;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;
import static com.yolt.providers.openbanking.ais.aibgroup.aibgb.AibGbDetailsProvider.PROVIDER_DISPLAY_NAME;
import static com.yolt.providers.openbanking.ais.aibgroup.aibgb.AibGbDetailsProvider.PROVIDER_KEY;
import static com.yolt.providers.openbanking.ais.aibgroup.common.auth.AibGroupAuthMeansBuilderV3.*;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INTERIMAVAILABLE;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.OPENINGAVAILABLE;

@Configuration
public class AibGBBeanConfigV2 {

    private static final String JWS_SIGNING_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
    private static final String ENDPOINT_VERSION = "/v3.1";
    private static final String LOCAL_INSTRUMENT = "UK.OBIE.FPS";

    private FetchDataService getFetchDataService(AibPropertiesV2 properties,
                                                 final Clock clock,
                                                 final ObjectMapper objectMapper) {
        ZoneId zoneId = ZoneId.of("Europe/London");
        DefaultAmountParser amountParser = new DefaultAmountParser();
        DefaultAccountReferenceTypeMapper accountReferenceTypeMapper = new DefaultAccountReferenceTypeMapper();
        DefaultCurrencyMapper currencyCodeMapper = new DefaultCurrencyMapper();
        DefaultBalanceMapper balanceMapper = new DefaultBalanceMapper();
        DefaultSchemeMapper schemeMapper = new DefaultSchemeMapper();
        DefaultDateTimeMapper zonedDateTimeMapper = new DefaultDateTimeMapper(zoneId);
        DefaultBalanceAmountMapper balanceAmountMapper = new DefaultBalanceAmountMapper(currencyCodeMapper, balanceMapper);
        return new DefaultFetchDataService(new DefaultRestClient(new ExternalPaymentRequestSigner(objectMapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256)), properties,
                new DefaultTransactionMapper(
                        new DefaultExtendedTransactionMapper(
                                accountReferenceTypeMapper,
                                new DefaultTransactionStatusMapper(),
                                balanceAmountMapper,
                                false,
                                zoneId),
                        zonedDateTimeMapper,
                        new PendingAsNullTransactionStatusMapper(),
                        amountParser,
                        new DefaultTransactionTypeMapper()),
                new DefaultDirectDebitMapper(zoneId, amountParser),
                new DefaultStandingOrderMapper(new DefaultPeriodMapper(), amountParser, schemeMapper, zonedDateTimeMapper),
                new AibGroupAccountMapperV3(() -> Arrays.asList(OPENINGAVAILABLE), () -> Arrays.asList(INTERIMAVAILABLE),
                        currencyCodeMapper,
                        new DefaultAccountIdMapper(),
                        new DefaultAccountTypeMapper(),
                        new CreditCardMapper(),
                        new AccountNumberMapper(schemeMapper),
                        new DefaultAccountNameMapper(account -> "AIB Account"),
                        new DefaultExtendedAccountMapper(accountReferenceTypeMapper,
                                currencyCodeMapper,
                                new DefaultExtendedBalancesMapper(
                                        balanceAmountMapper,
                                        new DefaultBalanceTypeMapper(),
                                        zoneId)),
                        balanceMapper,
                        new DefaultSupportedSchemeAccountFilter(),
                        clock), new DefaultAccountFilter(),
                new DefaultSupportedAccountsSupplier(),
                DefaultConsentWindow.DURATION,
                ENDPOINT_VERSION,
                clock);
    }

    private AuthenticationService getAuthenticationServiceV2(final AibPropertiesV2 properties,
                                                             final ExternalUserRequestTokenSigner tokenSigner,
                                                             final Clock clock) {
        return new DefaultAuthenticationService(properties.getOAuthAuthorizationUrl(),
                new AibGroupOAuth2ClientV2(properties, false),
                tokenSigner,
                new DefaultTokenClaimsProducer(new FapiCompliantJwtClaimsProducerDecorator(new DefaultJwtClaimsProducer(DefaultAuthMeans::getClientId,
                        properties.getLoginUrlAudience()))),
                clock);
    }

    private AccountRequestService getAccountRequestService(final ObjectMapper mapper,
                                                           final AuthenticationService authenticationService,
                                                           final String endpointVersion) {
        return new DefaultAccountAccessConsentRequestService(authenticationService,
                new DefaultRestClient(new ExternalPaymentRequestSigner(mapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256)),
                endpointVersion);
    }

    @Bean("AibDataProviderV6")
    public GenericBaseDataProvider getAibDataProviderV6(final AibPropertiesV2 properties,
                                                        @Qualifier("OpenBanking") final ObjectMapper objectMapper,
                                                        final MeterRegistry registry,
                                                        final Clock clock) {
        String jwsSigningAlgorithm = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
        ExternalUserRequestTokenSigner tokenSigner = new ExternalUserRequestTokenSigner(jwsSigningAlgorithm);
        AuthenticationService authenticationService = getAuthenticationServiceV2(properties, tokenSigner, clock);
        AccountRequestService accountRequestService = getAccountRequestService(objectMapper, authenticationService, ENDPOINT_VERSION);
        TokenScope tokenScope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.ACCOUNTS.getGrantScope())
                .authorizationUrlScope(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope())
                .build();
        return new GenericBaseDataProvider(getFetchDataService(properties, clock, objectMapper),
                accountRequestService,
                authenticationService,
                new AibGroupHttpClientFactoryV2(properties, registry, objectMapper),
                tokenScope,
                new ProviderIdentification(PROVIDER_KEY, PROVIDER_DISPLAY_NAME, ProviderVersion.VERSION_6),
                defaultAuthMeans -> AibGroupAuthMeansBuilderV3.createAuthenticationMeansForAis(defaultAuthMeans, PROVIDER_KEY),
                AibGroupAuthMeansBuilderV3.getTypedAuthenticationMeansSupplier(),
                new DefaultAccessMeansMapper(objectMapper),
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                () -> new ConsentValidityRules(Collections.singleton("Share Account Information"))
        );
    }

    @Bean("AibPaymentProviderV1")
    public GenericBasePaymentProviderV3 getAibPaymentProviderProvider(final AibPropertiesV2 properties,
                                                                      final MeterRegistry registry,
                                                                      final Clock clock,
                                                                      @Qualifier("OpenBanking") ObjectMapper objectMapper) {

        ProviderIdentification providerIdentification = new ProviderIdentification(PROVIDER_KEY, PROVIDER_DISPLAY_NAME, VERSION_1);
        ExternalUserRequestTokenSigner tokenSigner = new ExternalUserRequestTokenSigner(AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        AuthenticationService authenticationService = getAuthenticationServiceV2ForPis(properties, tokenSigner, clock);

        Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authMeansProvider =
                authenticationMeans -> AibGroupAuthMeansBuilderV3.createAuthenticationMeansForPis(authenticationMeans, PROVIDER_KEY);

        DateTimeAndUUIDBasedInstructionIdentificationSupplier instructionIdentificationSupplier =
                new DateTimeAndUUIDBasedInstructionIdentificationSupplier(clock);
        PaymentDataInitiationMapper ukDomesticDataInitiationMapper = new PaymentDataInitiationMapper(
                instructionIdentificationSupplier,
                new DefaultAmountFormatter(),
                new CamelCaseUkSchemeMapper()
        )
                .withDebtorAccount()
                .withLocalInstrument(LOCAL_INSTRUMENT)
                .validateAfterMapWith(new AibGroupPaymentRequestValidator());

        TokenScope tokenScope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.PAYMENTS.getAuthorizationUrlScope())
                .authorizationUrlScope(OpenBankingTokenScope.PAYMENTS.getAuthorizationUrlScope())
                .build();

        DefaultPaymentExecutionContextAdapterFactoryV2 executionContextAdapterFactory = new DefaultPaymentExecutionContextAdapterFactoryV2(
                providerIdentification,
                objectMapper,
                authMeansProvider,
                authenticationService,
                new AibGroupHttpClientFactoryV2(properties, registry, objectMapper),
                ukDomesticDataInitiationMapper,
                null,
                OBRisk1.PaymentContextCodeEnum.PARTYTOPARTY,
                () -> ENDPOINT_VERSION,
                new ExternalPaymentNoB64RequestSigner(objectMapper, JWS_SIGNING_ALGORITHM),
                clock,
                tokenScope,
                new GenericConsentResponseStatusMapper(),
                new GenericStatusResponseStatusMapper(),
                new GenericScheduledConsentResponseStatusMapper(),
                new GenericScheduledResponseStatusMapper()
        );

        return new GenericBasePaymentProviderV3(
                executionContextAdapterFactory.createInitiatePaymentExecutionContextAdapter(),
                executionContextAdapterFactory.createInitiateScheduledPaymentExecutionContextAdapter(),
                executionContextAdapterFactory.createSubmitPaymentExecutionContextAdapter(),
                executionContextAdapterFactory.createSubmitScheduledPaymentExecutionContextAdapter(),
                executionContextAdapterFactory.createStatusPaymentExecutionContextAdapter(),
                executionContextAdapterFactory.createStatusScheduledPaymentExecutionContextAdapter(),
                providerIdentification,
                AibGroupAuthMeansBuilderV3.getTypedAuthenticationMeansSupplier(),
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                new DefaultConsentValidityRulesSupplier().get(),
                new UkProviderStateDeserializer(objectMapper)
        );
    }

    private AuthenticationService getAuthenticationServiceV2ForPis(final AibPropertiesV2 properties,
                                                                   final ExternalUserRequestTokenSigner tokenSigner,
                                                                   final Clock clock) {
        return new NoCacheAuthenticationService(properties.getOAuthAuthorizationUrl(),
                new AibGroupOAuth2ClientV2(properties, true),
                tokenSigner,
                new DefaultTokenClaimsProducer(new FapiCompliantJwtClaimsProducerDecorator(new DefaultJwtClaimsProducer(DefaultAuthMeans::getClientId,
                        properties.getLoginUrlAudience()))),
                clock);
    }
}
