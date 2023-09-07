package com.yolt.providers.openbanking.ais.revolutgroup.revolut.beanconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV3;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.consentvalidity.DefaultConsentValidityRulesSupplier;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
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
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultConsentWindow;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.supportedaccounts.DefaultSupportedAccountsSupplier;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountNumberMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountIdMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountNameMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper.AccountReferenceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper.DefaultAccountReferenceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.amount.DefaultAmountParser;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.BalanceMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.DefaultBalanceMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balanceamount.DefaultBalanceAmountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balancetype.DefaultBalanceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.creditcard.CreditCardMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.currency.DefaultCurrencyMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedbalance.DefaultExtendedBalancesMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedtransaction.DefaultExtendedTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultAccessMeansMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.period.DefaultPeriodMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme.DefaultSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme.SchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.time.DefaultDateTimeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultDirectDebitMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultStandingOrderMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.DefaultTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.PendingAsNullTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactiontype.DefaultTransactionTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentRequestSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.revolutgroup.common.RevolutGroupDataProviderV7;
import com.yolt.providers.openbanking.ais.revolutgroup.common.RevolutPropertiesV2;
import com.yolt.providers.openbanking.ais.revolutgroup.common.auth.RevolutGbAuthMeansBuilderV2;
import com.yolt.providers.openbanking.ais.revolutgroup.common.http.RevolutHttpClientFactoryV2;
import com.yolt.providers.openbanking.ais.revolutgroup.common.oauth2.RevolutMutualTlsOauth2ClientV1;
import com.yolt.providers.openbanking.ais.revolutgroup.common.oauth2.tokenbodysupplier.RevolutBasicOauthTokenBodyProducer;
import com.yolt.providers.openbanking.ais.revolutgroup.common.pec.RevolutPaymentExecutionContextAdapterFactory;
import com.yolt.providers.openbanking.ais.revolutgroup.common.pec.RevolutPaymentRequestValidator;
import com.yolt.providers.openbanking.ais.revolutgroup.common.service.ais.RevolutFetchDataServiceV4;
import com.yolt.providers.openbanking.ais.revolutgroup.common.service.ais.mappers.RevolutAccountRequestServiceV4;
import com.yolt.providers.openbanking.ais.revolutgroup.common.service.ais.mappers.RevolutDefaultAccountMapperV2;
import com.yolt.providers.openbanking.ais.revolutgroup.common.service.ais.mappers.RevolutExtendedAccountMapperV3;
import com.yolt.providers.openbanking.ais.revolutgroup.common.service.ais.mappers.RevolutSupportedSchemeAccountFilter;
import com.yolt.providers.openbanking.ais.revolutgroup.common.signer.RevolutPaymentRequestSigner;
import com.yolt.providers.openbanking.ais.revolutgroup.revolut.service.RevolutAutoOnboardingServiceV3;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBRisk1;
import io.micrometer.core.instrument.MeterRegistry;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;
import static com.yolt.providers.openbanking.ais.revolutgroup.common.auth.RevolutGbAuthMeansBuilderV2.*;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INTERIMAVAILABLE;

@Configuration
public class RevolutGbBeanConfig {

    private static final String ENDPOINT_VERSION = "";
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/London");
    private static final String JWS_SIGNING_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
    private final UserRequestTokenSigner userRequestTokenSigner = new ExternalUserRequestTokenSigner(JWS_SIGNING_ALGORITHM);
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authenticationMeansFactoryFunction =
            RevolutGbAuthMeansBuilderV2::createAuthenticationMeans;
    private final Supplier<Map<String, TypedAuthenticationMeans>> typedAuthenticationMeansSupplier = () ->
    {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(RevolutGbAuthMeansBuilderV2.INSTITUTION_ID_NAME, TypedAuthenticationMeans.INSTITUTION_ID_STRING);
        typedAuthenticationMeans.put(RevolutGbAuthMeansBuilderV2.CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(RevolutGbAuthMeansBuilderV2.SIGNING_KEY_HEADER_ID_NAME, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
        typedAuthenticationMeans.put(RevolutGbAuthMeansBuilderV2.TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        typedAuthenticationMeans.put(RevolutGbAuthMeansBuilderV2.SOFTWARE_ID_NAME, TypedAuthenticationMeans.SOFTWARE_ID_STRING);
        typedAuthenticationMeans.put(RevolutGbAuthMeansBuilderV2.SOFTWARE_STATEMENT_ASSERTION_NAME, TypedAuthenticationMeans.SOFTWARE_STATEMENT_ASSERTION_STRING);
        typedAuthenticationMeans.put(RevolutGbAuthMeansBuilderV2.ORGANIZATION_ID_NAME, TypedAuthenticationMeans.ORGANIZATION_ID_STRING);
        typedAuthenticationMeans.put(RevolutGbAuthMeansBuilderV2.SIGNING_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(RevolutGbAuthMeansBuilderV2.TRANSPORT_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        return typedAuthenticationMeans;
    };

    private DefaultAccountAccessConsentRequestService getAccountRequestService(ObjectMapper objectMapper,
                                                                               AuthenticationService authenticationService) {
        return new RevolutAccountRequestServiceV4(
                authenticationService,
                getRestClient(objectMapper),
                ENDPOINT_VERSION);
    }

    private DefaultAuthenticationService getAuthenticationServiceV3(DefaultProperties properties, Clock clock, boolean isInPisFlow) {
        return new DefaultAuthenticationService(
                properties.getOAuthAuthorizationUrl(),
                new RevolutMutualTlsOauth2ClientV1(
                        properties.getOAuthTokenUrl(),
                        arg -> null,
                        new RevolutBasicOauthTokenBodyProducer(),
                        isInPisFlow
                ),
                userRequestTokenSigner,
                new DefaultTokenClaimsProducer(
                        new DefaultJwtClaimsProducer(
                                DefaultAuthMeans::getClientId,
                                properties.getAudience())),
                clock);
    }

    private DefaultRestClient getRestClient(ObjectMapper objectMapper) {
        return new DefaultRestClient(
                new ExternalPaymentRequestSigner(
                        objectMapper,
                        JWS_SIGNING_ALGORITHM));
    }

    private DefaultHttpClientFactory getHttpClientFactory(DefaultProperties properties, MeterRegistry registry, ObjectMapper objectMapper) {
        return new RevolutHttpClientFactoryV2(properties, registry, objectMapper);
    }

    @Bean("RevolutDataProviderV10")
    public GenericBaseDataProvider getRevolutGbDataProviderV3(RevolutPropertiesV2 properties,
                                                              MeterRegistry registry,
                                                              Clock clock,
                                                              @Qualifier("OpenBanking") ObjectMapper objectMapper) {
        ProviderIdentification providerIdentification = new ProviderIdentification(RevolutDetailsProvider.PROVIDER_KEY, RevolutDetailsProvider.PROVIDER_NAME, ProviderVersion.VERSION_10);
        Supplier<List<OBBalanceType1Code>> currentBalanceTypeSupplier = () -> List.of(INTERIMAVAILABLE);
        Supplier<List<OBBalanceType1Code>> availableBalanceTypeSupplier = () -> List.of(INTERIMAVAILABLE);
        Function<OBAccount6, String> accountNameFallback = account -> String.format("Revolut %s Account", account.getCurrency());
        AuthenticationService authenticationService = getAuthenticationServiceV3(properties, clock, false);
        AccountRequestService accountRequestService = getAccountRequestService(objectMapper, authenticationService);
        SchemeMapper schemeMapper = new DefaultSchemeMapper();
        Function<String, CurrencyCode> currencyMapper = new DefaultCurrencyMapper();
        BalanceMapper balanceMapper = new DefaultBalanceMapper();
        Function<String, BigDecimal> amountParser = new DefaultAmountParser();
        Function<String, ZonedDateTime> zonedDateTimeMapper = new DefaultDateTimeMapper(ZONE_ID);
        AccountReferenceTypeMapper accountReferenceTypeMapper = new DefaultAccountReferenceTypeMapper();
        DefaultSupportedAccountsSupplier supportedAccountSupplier = new DefaultSupportedAccountsSupplier();
        FetchDataService fetchDataService =
                new RevolutFetchDataServiceV4(
                        getRestClient(objectMapper),
                        properties,
                        new DefaultTransactionMapper(
                                new DefaultExtendedTransactionMapper(
                                        accountReferenceTypeMapper,
                                        new DefaultTransactionStatusMapper(),
                                        new DefaultBalanceAmountMapper(currencyMapper, balanceMapper),
                                        false,
                                        ZONE_ID),
                                zonedDateTimeMapper,
                                new PendingAsNullTransactionStatusMapper(),
                                new DefaultAmountParser(),
                                new DefaultTransactionTypeMapper()),
                        new DefaultDirectDebitMapper(ZONE_ID, amountParser),
                        new DefaultStandingOrderMapper(new DefaultPeriodMapper(), amountParser, schemeMapper, zonedDateTimeMapper),
                        new RevolutDefaultAccountMapperV2(
                                currentBalanceTypeSupplier,
                                availableBalanceTypeSupplier,
                                currencyMapper,
                                new DefaultAccountIdMapper(),
                                new DefaultAccountTypeMapper(),
                                new CreditCardMapper(),
                                new AccountNumberMapper(schemeMapper),
                                new DefaultAccountNameMapper(accountNameFallback),
                                balanceMapper,
                                new RevolutExtendedAccountMapperV3(
                                        accountReferenceTypeMapper,
                                        currencyMapper,
                                        new DefaultExtendedBalancesMapper(
                                                new DefaultBalanceAmountMapper(
                                                        currencyMapper,
                                                        new DefaultBalanceMapper()),
                                                new DefaultBalanceTypeMapper(),
                                                ZONE_ID)),
                                new RevolutSupportedSchemeAccountFilter(),
                                clock),
                        new DefaultAccountFilter(),
                        supportedAccountSupplier,
                        DefaultConsentWindow.DURATION,
                        ENDPOINT_VERSION,
                        clock);
        return new RevolutGroupDataProviderV7(
                fetchDataService,
                accountRequestService,
                authenticationService,
                getHttpClientFactory(properties, registry, objectMapper),
                TokenScope.builder()
                        .grantScope(OpenBankingTokenScope.ACCOUNTS.getGrantScope())
                        .authorizationUrlScope(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope())
                        .build(),
                providerIdentification,
                authenticationMeansFactoryFunction,
                typedAuthenticationMeansSupplier,
                new DefaultAccessMeansMapper<AccessMeans>(objectMapper),
                () -> HsmUtils.getKeyRequirements(RevolutGbAuthMeansBuilderV2.SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(RevolutGbAuthMeansBuilderV2.TRANSPORT_PRIVATE_KEY_ID_NAME, RevolutGbAuthMeansBuilderV2.TRANSPORT_CERTIFICATE_NAME),
                () -> ConsentValidityRules.EMPTY_RULES_SET,
                new RevolutAutoOnboardingServiceV3(
                        properties,
                        clock,
                        RevolutGbAuthMeansBuilderV2.CLIENT_ID_NAME),
                RevolutGbAuthMeansBuilderV2.CLIENT_ID_NAME,
                authenticationMeansFactoryFunction);
    }

    @Bean("RevolutPaymentProviderV1")
    public GenericBasePaymentProviderV3 getPaymentProviderProvider(final RevolutPropertiesV2 properties,
                                                                   final MeterRegistry registry,
                                                                   final Clock clock,
                                                                   @Qualifier("OpenBanking") ObjectMapper objectMapper) {

        ProviderIdentification providerIdentification = new ProviderIdentification(RevolutDetailsProvider.PROVIDER_KEY, RevolutDetailsProvider.PROVIDER_NAME, VERSION_1);
        AuthenticationService authenticationService = getAuthenticationServiceV3(properties, clock, true);

        Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authMeansProvider =
                authenticationMeans -> RevolutGbAuthMeansBuilderV2.createAuthenticationMeans(authenticationMeans);

        DateTimeAndUUIDBasedInstructionIdentificationSupplier instructionIdentificationSupplier =
                new DateTimeAndUUIDBasedInstructionIdentificationSupplier(clock);
        PaymentDataInitiationMapper ukDomesticDataInitiationMapper = new PaymentDataInitiationMapper(
                instructionIdentificationSupplier,
                new DefaultAmountFormatter(),
                new CamelCaseUkSchemeMapper())
                .withDebtorAccount()
                .validateAfterMapWith(new RevolutPaymentRequestValidator());

        TokenScope tokenScope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.PAYMENTS.getGrantScope())
                .authorizationUrlScope(OpenBankingTokenScope.PAYMENTS.getAuthorizationUrlScope())
                .build();

        RevolutPaymentExecutionContextAdapterFactory executionContextAdapterFactory = new RevolutPaymentExecutionContextAdapterFactory(
                providerIdentification,
                objectMapper,
                authMeansProvider,
                authenticationService,
                getHttpClientFactory(properties, registry, objectMapper),
                ukDomesticDataInitiationMapper,
                null,
                OBRisk1.PaymentContextCodeEnum.PARTYTOPARTY,
                () -> ENDPOINT_VERSION,
                new RevolutPaymentRequestSigner(objectMapper, JWS_SIGNING_ALGORITHM, "openbanking.org.uk"),
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
                typedAuthenticationMeansSupplier,
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                new DefaultConsentValidityRulesSupplier().get(),
                new UkProviderStateDeserializer(objectMapper)
        );
    }
}
