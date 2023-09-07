package com.yolt.providers.openbanking.ais.tescobank.beanconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.consentvalidity.DefaultConsentValidityRulesSupplier;
import com.yolt.providers.openbanking.ais.generic2.domain.LoginInfoState;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.oauth2.implementations.DefaultMutualTlsOauth2Client;
import com.yolt.providers.openbanking.ais.generic2.pec.DefaultPaymentExecutionContextAdapterFactory;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.amount.DefaultAmountFormatter;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.datainitiation.PaymentDataInitiationMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.instructionidentification.DateTimeAndUUIDBasedInstructionIdentificationSupplier;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme.CamelCaseUkSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericConsentResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericStatusResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.DefaultConsentPermissions;
import com.yolt.providers.openbanking.ais.generic2.service.ais.exceptionhandler.DefaultFetchDataExceptionHandler;
import com.yolt.providers.openbanking.ais.generic2.service.ais.exceptionhandler.FetchDataExceptionHandler;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataServiceV4;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.frombookingdatetimeformatter.DefaultFromBookingDateTimeFormatter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.supportedaccounts.DefaultSupportedAccountsSupplier;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountMapperV3;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountNameMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountNumberMapperV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountTypeMapper;
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
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultAccessMeansStateMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultAccessMeansStateProvider;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultLoginInfoStateMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.parties.DefaultPartiesMapper;
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
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultPartiesRestClient;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentRequestSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.tescobank.TescoBankBaseDataProviderV4;
import com.yolt.providers.openbanking.ais.tescobank.TescoBankPropertiesV2;
import com.yolt.providers.openbanking.ais.tescobank.auth.TescoBankAuthMeansBuilderV3;
import com.yolt.providers.openbanking.ais.tescobank.oauth2.tokenbodysupplier.TescoBankOauthTokenBodyProducer;
import com.yolt.providers.openbanking.ais.tescobank.pec.common.mapper.validator.TescoBankPaymentRequestValidator;
import com.yolt.providers.openbanking.ais.tescobank.service.ais.exceptionhandler.TescoFetchAccountDetailsExceptionHandler;
import com.yolt.providers.openbanking.ais.tescobank.service.ais.exceptionhandler.TescoFetchAccountsExceptionHandler;
import com.yolt.providers.openbanking.ais.tescobank.service.ais.mappers.account.TescoBankAccountIdMapper;
import com.yolt.providers.openbanking.ais.tescobank.service.autoonboarding.TescoBankAutoOnboardingServiceV2;
import com.yolt.providers.openbanking.ais.tescobank.service.restclient.TescoBankRestClientV2;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBRisk1;
import io.micrometer.core.instrument.MeterRegistry;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import static com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultPermissions.DEFAULT_PERMISSIONS;
import static com.yolt.providers.openbanking.ais.tescobank.auth.TescoBankAuthMeansBuilderV3.*;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.*;

@Configuration
public class TescoBankBeanConfigV2 {

    private static final String IDENTIFIER = "TESCO_BANK";
    private static final String DISPLAY_NAME = "Tesco Bank";
    private static final String ACCOUNT_NAME_FALLBACK = "Tesco Bank Account";
    private static final String ENDPOINT_VERSION = "/v3.1";
    private static final String REGISTRATION_AUTH_METHOD = "tls_client_auth";
    private static final String JWS_SIGNING_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/London");
    private static final Duration CONSENT_WINDOW_DURATION = Duration.ofMinutes(5);

    @Bean("TescoBankPaymentProviderV5")
    public GenericBasePaymentProviderV2 getTescoBankPaymentProviderV5(TescoBankPropertiesV2 properties,
                                                                      MeterRegistry registry,
                                                                      Clock clock,
                                                                      @Qualifier("OpenBanking") ObjectMapper objectMapper) {
        ProviderIdentification providerIdentification = new ProviderIdentification(IDENTIFIER, DISPLAY_NAME, ProviderVersion.VERSION_5);
        TokenScope tokenScope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.PAYMENTS.getGrantScope())
                .authorizationUrlScope(OpenBankingTokenScope.PAYMENTS.getAuthorizationUrlScope())
                .build();
        DefaultPaymentExecutionContextAdapterFactory pecAdapterFactory = createPaymentExecutionContextAdapterFactory(providerIdentification,
                objectMapper,
                properties,
                registry,
                tokenScope,
                clock);

        return new GenericBasePaymentProviderV2(pecAdapterFactory.createInitiatePaymentExecutionContextAdapter(),
                pecAdapterFactory.createSubmitPaymentExecutionContextAdapter(),
                pecAdapterFactory.createStatusPaymentExecutionContextAdapter(),
                providerIdentification,
                TescoBankAuthMeansBuilderV3.getTypedAuthenticationMeansForPIS(),
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                new DefaultConsentValidityRulesSupplier().get());
    }

    private DefaultPaymentExecutionContextAdapterFactory createPaymentExecutionContextAdapterFactory(ProviderIdentification providerIdentification,
                                                                                                     ObjectMapper objectMapper,
                                                                                                     TescoBankPropertiesV2 properties,
                                                                                                     MeterRegistry registry,
                                                                                                     TokenScope tokenScope,
                                                                                                     Clock clock) {
        AuthenticationService authenticationService = getAuthenticationService(properties, clock, true);
        PaymentDataInitiationMapper ukDomesticDataInitiationMapper = createCommonUkDomesticDataInitiationMapper(clock)
                .validateAfterMapWith(new TescoBankPaymentRequestValidator());
        DefaultHttpClientFactory httpClientFactory = getHttpClientFactory(properties, registry, objectMapper);
        ExternalPaymentRequestSigner paymentRequestSigner = new ExternalPaymentRequestSigner(objectMapper, JWS_SIGNING_ALGORITHM);


        return new DefaultPaymentExecutionContextAdapterFactory(providerIdentification,
                objectMapper,
                TescoBankAuthMeansBuilderV3::createAuthenticationMeans,
                authenticationService,
                httpClientFactory,
                ukDomesticDataInitiationMapper,
                OBRisk1.PaymentContextCodeEnum.PARTYTOPARTY,
                () -> ENDPOINT_VERSION,
                paymentRequestSigner,
                clock,
                tokenScope,
                new GenericConsentResponseStatusMapper(),
                new GenericStatusResponseStatusMapper());
    }

    private PaymentDataInitiationMapper createCommonUkDomesticDataInitiationMapper(Clock clock) {
        return new PaymentDataInitiationMapper(new DateTimeAndUUIDBasedInstructionIdentificationSupplier(clock),
                new DefaultAmountFormatter(),
                new CamelCaseUkSchemeMapper())
                .withDebtorAccount();
    }

    private DefaultAuthenticationService getAuthenticationService(TescoBankPropertiesV2 properties,
                                                                  Clock clock,
                                                                  boolean isInPisFlow) {
        UserRequestTokenSigner userRequestTokenSigner = new ExternalUserRequestTokenSigner(JWS_SIGNING_ALGORITHM);
        return new DefaultAuthenticationService(
                properties.getOAuthAuthorizationUrl(),
                new DefaultMutualTlsOauth2Client(
                        properties,
                        new TescoBankOauthTokenBodyProducer(),
                        isInPisFlow),
                userRequestTokenSigner,
                new DefaultTokenClaimsProducer(
                        new DefaultJwtClaimsProducer(
                                DefaultAuthMeans::getClientId,
                                properties.getAudience()
                        )
                ),
                clock
        );
    }

    private TescoBankRestClientV2 getRestClient(ObjectMapper objectMapper) {
        return new TescoBankRestClientV2(
                new ExternalPaymentRequestSigner(
                        objectMapper,
                        JWS_SIGNING_ALGORITHM));
    }

    private DefaultAccountAccessConsentRequestServiceV2 getAccountRequestServiceV2(AuthenticationService authenticationService,
                                                                                   RestClient restClient) {
        return new DefaultAccountAccessConsentRequestServiceV2(
                authenticationService,
                restClient,
                ENDPOINT_VERSION,
                DEFAULT_PERMISSIONS);
    }

    private DefaultHttpClientFactory getHttpClientFactory(TescoBankPropertiesV2 properties,
                                                          MeterRegistry registry,
                                                          ObjectMapper objectMapper) {
        return new DefaultHttpClientFactory(properties, registry, objectMapper);
    }

    private FetchDataServiceV2 getFetchDataServiceV2(ObjectMapper objectMapper,
                                                     Clock clock,
                                                     TescoBankPropertiesV2 properties) {
        SchemeMapper schemeMapper = new DefaultSchemeMapper();
        Function<String, CurrencyCode> defaultCurrencyMapper = new DefaultCurrencyMapper();
        BalanceMapper defaultBalanceMapper = new DefaultBalanceMapper();
        Function<String, BigDecimal> amountParser = new DefaultAmountParser();
        Function<String, ZonedDateTime> zonedDateTimeMapper = new DefaultDateTimeMapper(ZONE_ID);
        AccountReferenceTypeMapper accountReferenceTypeMapper = new DefaultAccountReferenceTypeMapper();
        DefaultAccountTypeMapper defaultAccountTypeMapper = new DefaultAccountTypeMapper();
        DefaultSupportedSchemeAccountFilter defaultSupportedSchemeAccountFilter = new DefaultSupportedSchemeAccountFilter();
        DefaultBalanceAmountMapper defaultBalanceAmountMapper = new DefaultBalanceAmountMapper(defaultCurrencyMapper, defaultBalanceMapper);
        FetchDataExceptionHandler fetchDataExceptionHandler = new DefaultFetchDataExceptionHandler();
        return new DefaultFetchDataServiceV4(
                getRestClient(objectMapper),
                new DefaultPartiesRestClient(),
                properties,
                new DefaultTransactionMapper(
                        new DefaultExtendedTransactionMapper(
                                accountReferenceTypeMapper,
                                new DefaultTransactionStatusMapper(),
                                defaultBalanceAmountMapper,
                                false,
                                ZONE_ID),
                        zonedDateTimeMapper,
                        new PendingAsNullTransactionStatusMapper(),
                        amountParser,
                        new DefaultTransactionTypeMapper()),
                new DefaultDirectDebitMapper(ZONE_ID, amountParser),
                new DefaultStandingOrderMapper(new DefaultPeriodMapper(), amountParser, schemeMapper, zonedDateTimeMapper),
                new DefaultPartiesMapper(),
                new DefaultAccountMapperV3(
                        () -> Collections.singletonList(INTERIMBOOKED),
                        () -> Collections.singletonList(INTERIMAVAILABLE),
                        () -> Collections.singletonList(CLOSINGBOOKED),
                        () -> Collections.singletonList(INTERIMAVAILABLE),
                        defaultCurrencyMapper,
                        new TescoBankAccountIdMapper(defaultAccountTypeMapper, defaultSupportedSchemeAccountFilter),
                        defaultAccountTypeMapper,
                        new CreditCardMapper(),
                        new DefaultAccountNumberMapperV2(schemeMapper),
                        new DefaultAccountNameMapper(account -> ACCOUNT_NAME_FALLBACK),
                        defaultBalanceMapper,
                        new DefaultExtendedAccountMapper(
                                accountReferenceTypeMapper,
                                defaultCurrencyMapper,
                                new DefaultExtendedBalancesMapper(
                                        defaultBalanceAmountMapper,
                                        new DefaultBalanceTypeMapper(),
                                        ZONE_ID)),
                        defaultSupportedSchemeAccountFilter,
                        clock),
                new DefaultFromBookingDateTimeFormatter(),
                new DefaultAccountFilter(),
                new DefaultSupportedAccountsSupplier(),
                CONSENT_WINDOW_DURATION,
                ENDPOINT_VERSION,
                clock,
                new TescoFetchAccountsExceptionHandler(fetchDataExceptionHandler),
                new TescoFetchAccountDetailsExceptionHandler(fetchDataExceptionHandler));
    }

    @Bean("TescoBankDataProviderV7")
    public TescoBankBaseDataProviderV4 getTescoBankDataProviderV7(TescoBankPropertiesV2 properties,
                                                                  MeterRegistry registry,
                                                                  Clock clock,
                                                                  @Qualifier("OpenBanking") ObjectMapper objectMapper) {
        ProviderIdentification providerIdentification = new ProviderIdentification(IDENTIFIER, DISPLAY_NAME, ProviderVersion.VERSION_7);
        AuthenticationService authenticationService = getAuthenticationService(properties, clock, false);
        TescoBankRestClientV2 restClient = getRestClient(objectMapper);
        AccountRequestService accountRequestService = getAccountRequestServiceV2(authenticationService, restClient);
        FetchDataServiceV2 fetchDataService = getFetchDataServiceV2(objectMapper, clock, properties);
        return new TescoBankBaseDataProviderV4(
                fetchDataService,
                accountRequestService,
                authenticationService,
                getHttpClientFactory(properties, registry, objectMapper),
                TokenScope.builder()
                        .grantScope(OpenBankingTokenScope.ACCOUNTS.getGrantScope())
                        .authorizationUrlScope(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope())
                        .build(),
                providerIdentification,
                TescoBankAuthMeansBuilderV3::createAuthenticationMeans,
                TescoBankAuthMeansBuilderV3.getTypedAuthenticationMeansForAIS(),
                new DefaultAccessMeansStateMapper<>(objectMapper),
                new DefaultAccessMeansStateProvider(),
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                () -> new ConsentValidityRules(Set.of("Securely connecting - Tesco Bank", "Securely connecting to Tesco Bank")),
                new DefaultLoginInfoStateMapper<>(objectMapper),
                permissions -> new LoginInfoState(permissions),
                new DefaultConsentPermissions(DEFAULT_PERMISSIONS),
                new TescoBankAutoOnboardingServiceV2(
                        restClient,
                        JWS_SIGNING_ALGORITHM,
                        REGISTRATION_AUTH_METHOD,
                        properties)
        );
    }
}
