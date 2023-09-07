package com.yolt.providers.openbanking.ais.virginmoney.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper.DefaultUkRemittanceInformationMapper;
import com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper.LocalInstrumentUkPaymentMapperDecorator;
import com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper.WithoutDebtorUkPaymentMapper;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProvider;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.ExtendedJwtClaimProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.oauth2.implementations.DefaultClientSecretBasicOauth2Client;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme.CamelCaseUkSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme.UkSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultConsentWindow;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.supportedaccounts.DefaultSupportedAccountsSupplier;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.*;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.schemesupport.DefaultSupportedSchemeAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper.AccountReferenceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper.DefaultAccountReferenceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.amount.DefaultAmountParser;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.BalanceMapper;
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
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme.SchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.time.DefaultDateTimeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultDirectDebitMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultStandingOrderMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.DefaultTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.PendingAsNullTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactiontype.DefaultTransactionTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.pis.paymentservice.DefaultUkDomesticPaymentService;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentRequestSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.virginmoney.VirginMoneyDataProviderV5;
import com.yolt.providers.openbanking.ais.virginmoney.auth.VirginMoneyAuthMeansBuilderV4;
import com.yolt.providers.openbanking.ais.virginmoney.http.VirginMoneyHttpClientFactoryV2;
import com.yolt.providers.openbanking.ais.virginmoney.restclient.VirginMoneyRestClientV2;
import com.yolt.providers.openbanking.ais.virginmoney.service.VirginMoneyAutoOnboardingServiceV3;
import com.yolt.providers.openbanking.ais.virginmoney.service.ais.mappers.VirginMoneyAvailableCreditCardBalanceMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code;
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
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_4;
import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_5;
import static com.yolt.providers.openbanking.ais.virginmoney.auth.VirginMoneyAuthMeansBuilderV4.*;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INTERIMAVAILABLE;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INTERIMBOOKED;

@Configuration
public class VirginMoneyBeanConfigV3 {
    private static final String IDENTIFIER = "VIRGIN_MONEY";
    private static final String DISPLAY_NAME = "Virgin Money Credit card";
    private static final String ENDPOINT_VERSION = "/v3.1";
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/London");
    private static final String JWS_SIGNING_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
    private final UserRequestTokenSigner userRequestTokenSigner = new ExternalUserRequestTokenSigner(JWS_SIGNING_ALGORITHM);

    private AccountRequestService getAccountRequestService(ObjectMapper objectMapper,
                                                           AuthenticationService authenticationService) {
        return new DefaultAccountAccessConsentRequestService(
                authenticationService,
                getRestClient(objectMapper),
                ENDPOINT_VERSION);
    }

    private DefaultAuthenticationService getAuthenticationService(DefaultProperties properties, Clock clock, boolean isInPisFlow) {
        return new DefaultAuthenticationService(
                properties.getOAuthAuthorizationUrl(),
                new DefaultClientSecretBasicOauth2Client(properties, isInPisFlow),
                userRequestTokenSigner,
                new DefaultTokenClaimsProducer(
                        new ExtendedJwtClaimProducer(
                                DefaultAuthMeans::getClientId,
                                properties.getAudience())),
                clock);
    }

    private DefaultRestClient getRestClient(ObjectMapper objectMapper) {
        return new VirginMoneyRestClientV2(
                new ExternalPaymentRequestSigner(
                        objectMapper,
                        JWS_SIGNING_ALGORITHM));
    }

    private DefaultHttpClientFactory getHttpClientFactory(DefaultProperties properties, MeterRegistry registry, ObjectMapper objectMapper) {
        return new VirginMoneyHttpClientFactoryV2(properties, registry, objectMapper);
    }

    @Bean("VirginMoneyDataProviderV5")
    public GenericBaseDataProvider defaultBuild(VirginMoneyPropertiesV2 properties,
                                                MeterRegistry registry,
                                                Clock clock,
                                                @Qualifier("OpenBanking") ObjectMapper objectMapper) {
        ProviderIdentification providerIdentification = new ProviderIdentification(IDENTIFIER, DISPLAY_NAME, VERSION_5);
        Supplier<List<OBBalanceType1Code>> currentBalanceTypeSupplier = () -> Arrays.asList(INTERIMBOOKED);
        Supplier<List<OBBalanceType1Code>> availableBalanceTypeSupplier = () -> Arrays.asList(INTERIMAVAILABLE);
        Function<OBAccount6, String> accountNameFallback = any -> "Virgin Money Account";
        Supplier<List<OBBalanceType1Code>> getCurrentBalanceTypeForCreditCard = () -> Arrays.asList(INTERIMAVAILABLE, INTERIMBOOKED);
        Supplier<List<OBBalanceType1Code>> getAvailableBalanceTypeForCreditCard = () -> Arrays.asList(INTERIMAVAILABLE);
        AuthenticationService authenticationService = getAuthenticationService(properties, clock, false);
        AccountRequestService accountRequestService = getAccountRequestService(objectMapper, authenticationService);
        SchemeMapper schemeMapper = new DefaultSchemeMapper();
        Function<String, CurrencyCode> currencyMapper = new DefaultCurrencyMapper();
        DefaultBalanceMapper defaultBalanceMapper = new DefaultBalanceMapper();
        BalanceMapper currentCreditCardBalanceMapper = defaultBalanceMapper;
        Function<String, BigDecimal> amountParser = new DefaultAmountParser();
        Function<String, ZonedDateTime> zonedDateTimeMapper = new DefaultDateTimeMapper(ZONE_ID);
        AccountReferenceTypeMapper accountReferenceTypeMapper = new DefaultAccountReferenceTypeMapper();
        FetchDataService fetchDataService =
                new DefaultFetchDataService(
                        getRestClient(objectMapper),
                        properties,
                        new DefaultTransactionMapper(
                                new DefaultExtendedTransactionMapper(
                                        accountReferenceTypeMapper,
                                        new DefaultTransactionStatusMapper(),
                                        new DefaultBalanceAmountMapper(currencyMapper, defaultBalanceMapper),
                                        false,
                                        ZONE_ID),
                                zonedDateTimeMapper,
                                new PendingAsNullTransactionStatusMapper(),
                                new DefaultAmountParser(),
                                new DefaultTransactionTypeMapper()),
                        new DefaultDirectDebitMapper(ZONE_ID, amountParser),
                        new DefaultStandingOrderMapper(new DefaultPeriodMapper(), amountParser, schemeMapper, zonedDateTimeMapper),
                        new DefaultAccountMapperV2(
                                currentBalanceTypeSupplier,
                                availableBalanceTypeSupplier,
                                getCurrentBalanceTypeForCreditCard,
                                getAvailableBalanceTypeForCreditCard,
                                currencyMapper,
                                new DefaultAccountIdMapper(),
                                new DefaultAccountTypeMapper(),
                                new CreditCardMapper(),
                                new AccountNumberMapper(schemeMapper),
                                new DefaultAccountNameMapper(accountNameFallback),
                                defaultBalanceMapper,
                                new VirginMoneyAvailableCreditCardBalanceMapper(currentCreditCardBalanceMapper, getCurrentBalanceTypeForCreditCard),
                                defaultBalanceMapper,
                                currentCreditCardBalanceMapper,
                                new DefaultExtendedAccountMapper(
                                        accountReferenceTypeMapper,
                                        currencyMapper,
                                        new DefaultExtendedBalancesMapper(
                                                new DefaultBalanceAmountMapper(
                                                        currencyMapper,
                                                        new DefaultBalanceMapper()),
                                                new DefaultBalanceTypeMapper(),
                                                ZONE_ID)),
                                new DefaultSupportedSchemeAccountFilter(),
                                clock),
                        new DefaultAccountFilter(),
                        new DefaultSupportedAccountsSupplier(),
                        DefaultConsentWindow.DURATION,
                        ENDPOINT_VERSION,
                        clock);
        return new VirginMoneyDataProviderV5(
                fetchDataService,
                accountRequestService,
                authenticationService,
                getHttpClientFactory(properties, registry, objectMapper),
                TokenScope.builder()
                        .grantScope(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope())
                        .authorizationUrlScope(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope())
                        .build(),
                providerIdentification,
                VirginMoneyAuthMeansBuilderV4::createAuthenticationMeansForAis,
                () -> {
                    Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
                    typedAuthenticationMeans.put(INSTITUTION_ID_NAME, TypedAuthenticationMeans.INSTITUTION_ID_STRING);
                    typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
                    typedAuthenticationMeans.put(CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
                    typedAuthenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
                    typedAuthenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
                    typedAuthenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
                    typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
                    typedAuthenticationMeans.put(SOFTWARE_STATEMENT_ASSERTION_NAME, TypedAuthenticationMeans.SOFTWARE_STATEMENT_ASSERTION_STRING);
                    typedAuthenticationMeans.put(SOFTWARE_ID_NAME, TypedAuthenticationMeans.SOFTWARE_ID_STRING);
                    return typedAuthenticationMeans;
                },
                new DefaultAccessMeansMapper<AccessMeans>(objectMapper),
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                () -> {
                    Set<String> keywords = new HashSet<>();
                    keywords.add("React App");
                    return new ConsentValidityRules(keywords);
                },
                new VirginMoneyAutoOnboardingServiceV3(properties));
    }

    @Bean("VirginMoneyPaymentProviderV4")
    public GenericBasePaymentProvider paymentBuild(VirginMoneyPropertiesV2 properties,
                                                   MeterRegistry registry,
                                                   Clock clock,
                                                   @Qualifier("OpenBanking") ObjectMapper objectMapper) {
        ProviderIdentification providerIdentification = new ProviderIdentification(IDENTIFIER, DISPLAY_NAME, VERSION_4);
        AuthenticationService authenticationService = getAuthenticationService(properties, clock, true);
        DefaultRestClient restClient = getRestClient(objectMapper);
        UkSchemeMapper ukSchemeMapper = new CamelCaseUkSchemeMapper();
        String localInstrument = "UK.OBIE.FPS";
        return new GenericBasePaymentProvider(
                new DefaultUkDomesticPaymentService(
                        authenticationService,
                        restClient,
                        objectMapper,
                        new LocalInstrumentUkPaymentMapperDecorator(
                                new WithoutDebtorUkPaymentMapper(
                                        new DefaultUkRemittanceInformationMapper(),
                                        ukSchemeMapper,
                                        clock),
                                localInstrument),
                        ENDPOINT_VERSION),
                authenticationService,
                getHttpClientFactory(properties, registry, objectMapper),
                TokenScope.builder()
                        .grantScope(OpenBankingTokenScope.PAYMENTS.getGrantScope())
                        .authorizationUrlScope(OpenBankingTokenScope.PAYMENTS.getAuthorizationUrlScope())
                        .build(),
                providerIdentification,
                VirginMoneyAuthMeansBuilderV4::createAuthenticationMeansForPis,
                () -> {
                    Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();

                    typedAuthenticationMeans.put(INSTITUTION_ID_NAME, TypedAuthenticationMeans.INSTITUTION_ID_STRING);
                    typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
                    typedAuthenticationMeans.put(CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
                    typedAuthenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
                    typedAuthenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
                    typedAuthenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
                    typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
                    typedAuthenticationMeans.put(ORGANIZATION_ID_NAME, TypedAuthenticationMeans.ORGANIZATION_ID_STRING);
                    typedAuthenticationMeans.put(SOFTWARE_ID_NAME, TypedAuthenticationMeans.SOFTWARE_ID_STRING);
                    return typedAuthenticationMeans;
                },
                Optional::empty,
                Optional::empty);
    }
}
