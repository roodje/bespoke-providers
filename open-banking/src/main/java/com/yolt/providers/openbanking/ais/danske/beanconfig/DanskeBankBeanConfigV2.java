package com.yolt.providers.openbanking.ais.danske.beanconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.danske.DanskeBankDataProviderV7;
import com.yolt.providers.openbanking.ais.danske.http.DanskeBankHttpClientFactoryV2;
import com.yolt.providers.openbanking.ais.danske.oauth2.DanskeAuthMeansBuilderV3;
import com.yolt.providers.openbanking.ais.danske.oauth2.DanskeOauthTokenBodyProducer;
import com.yolt.providers.openbanking.ais.danske.pec.DanskePaymentRequestValidator;
import com.yolt.providers.openbanking.ais.danske.service.autoonboarding.DanskeBankAutoOnboardingServiceV2;
import com.yolt.providers.openbanking.ais.danske.service.claim.DanskeJwtClaimProducer;
import com.yolt.providers.openbanking.ais.danske.service.restclient.RegistrationRestClientV2;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV3;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.oauth2.BasicOauthClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.Oauth2Client;
import com.yolt.providers.openbanking.ais.generic2.pec.DefaultPaymentExecutionContextAdapterFactoryV2;
import com.yolt.providers.openbanking.ais.generic2.pec.common.UkProviderStateDeserializer;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.amount.DefaultAmountFormatter;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.datainitiation.PaymentDataInitiationMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.instructionidentification.DateTimeAndUUIDBasedInstructionIdentificationSupplier;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme.CamelCaseUkSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericConsentResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericStatusResponseStatusMapper;
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
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentNoKidRequestSignerDecorator;
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
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;
import static com.yolt.providers.openbanking.ais.danske.oauth2.DanskeAuthMeansBuilderV3.*;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INTERIMAVAILABLE;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INTERIMBOOKED;
import static org.jose4j.jws.AlgorithmIdentifiers.RSA_PSS_USING_SHA256;

@Configuration
public class DanskeBankBeanConfigV2 {

    public static final String PROVIDER_KEY = "DANSKEBANK";
    public static final String DISPLAY_NAME = "Danske Bank";
    private static final String ENDPOINT_VERSION = "/v3.1";

    private FetchDataService getFetchDataService(DanskeBankPropertiesV4 properties,
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
        return new DefaultFetchDataService(new DefaultRestClient(new ExternalPaymentNoKidRequestSignerDecorator(objectMapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256, new ExternalPaymentNoB64RequestSigner(objectMapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256))), properties,
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
                new DefaultAccountMapper(() -> Arrays.asList(INTERIMBOOKED), () -> Arrays.asList(INTERIMAVAILABLE),
                        currencyCodeMapper,
                        new DefaultAccountIdMapper(),
                        new DefaultAccountTypeMapper(),
                        new CreditCardMapper(),
                        new AccountNumberMapper(schemeMapper),
                        new DefaultAccountNameMapper(account -> "Danske Bank Account"),
                        balanceMapper,
                        new DefaultExtendedAccountMapper(accountReferenceTypeMapper,
                                currencyCodeMapper,
                                new DefaultExtendedBalancesMapper(
                                        balanceAmountMapper,
                                        new DefaultBalanceTypeMapper(),
                                        zoneId)),
                        new DefaultSupportedSchemeAccountFilter(),
                        clock),
                new DefaultAccountFilter(),
                new DefaultSupportedAccountsSupplier(),
                DefaultConsentWindow.DURATION,
                ENDPOINT_VERSION,
                clock);
    }

    private AuthenticationService getAuthenticationService(final DanskeBankPropertiesV4 properties,
                                                           final ExternalUserRequestTokenSigner tokenSigner,
                                                           final Clock clock,
                                                           boolean isInPisFlow) {
        Oauth2Client oauth2Client = new BasicOauthClient<>(properties.getOAuthTokenUrl(),
                defaultAuthMeans -> null,
                new DanskeOauthTokenBodyProducer(),
                isInPisFlow);
        return new DefaultAuthenticationService(properties.getOAuthAuthorizationUrl(),
                oauth2Client,
                tokenSigner,
                new DefaultTokenClaimsProducer(new DanskeJwtClaimProducer(new DefaultJwtClaimsProducer(DefaultAuthMeans::getClientId,
                        properties.getAudience()), 10)),
                clock);
    }

    private AccountRequestService getAccountRequestService(final ObjectMapper mapper,
                                                           final AuthenticationService authenticationService,
                                                           final String endpointVersion) {
        return new DefaultAccountAccessConsentRequestService(authenticationService,
                new DefaultRestClient(new ExternalPaymentRequestSigner(mapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256)),
                endpointVersion);
    }

    public DanskeBankAutoOnboardingServiceV2 getAutoOnboardingService(final ObjectMapper mapper,
                                                                      final DanskeBankPropertiesV4 properties) {
        RegistrationRestClientV2 registrationRestClient = new RegistrationRestClientV2(new ExternalPaymentRequestSigner(mapper, RSA_PSS_USING_SHA256));
        String authMethod = "tls_client_auth";
        return new DanskeBankAutoOnboardingServiceV2(
                registrationRestClient,
                AlgorithmIdentifiers.RSA_PSS_USING_SHA256,
                authMethod,
                properties
        );
    }

    @Bean("DanskeDataProviderV7")
    public DanskeBankDataProviderV7 getDanskeDataProviderV7(final DanskeBankPropertiesV4 properties,
                                                            final Clock clock,
                                                            @Qualifier("OpenBanking") final ObjectMapper objectMapper,
                                                            final MeterRegistry registry) {
        String jwsSigningAlgorithm = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
        ExternalUserRequestTokenSigner tokenSigner = new ExternalUserRequestTokenSigner(jwsSigningAlgorithm);
        AuthenticationService authenticationService = getAuthenticationService(properties, tokenSigner, clock, false);
        AccountRequestService accountRequestService = getAccountRequestService(objectMapper, authenticationService, ENDPOINT_VERSION);
        TokenScope tokenScope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.ACCOUNTS.getGrantScope())
                .authorizationUrlScope(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope())
                .build();
        return new DanskeBankDataProviderV7(getFetchDataService(properties, clock, objectMapper),
                accountRequestService,
                authenticationService,
                new DanskeBankHttpClientFactoryV2(properties, registry, objectMapper),
                tokenScope,
                new ProviderIdentification(PROVIDER_KEY, DISPLAY_NAME, ProviderVersion.VERSION_7),
                defaultAuthMeans -> DanskeAuthMeansBuilderV3.createDefaultAuthenticationMeans(defaultAuthMeans, PROVIDER_KEY),
                getTypedAuthenticationMeansForAIS(),
                new DefaultAccessMeansMapper(objectMapper),
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                () -> new ConsentValidityRules(new HashSet<>(Arrays.asList("Welcome to Danske Bank"))),
                getAutoOnboardingService(objectMapper, properties)
        );
    }

    @Bean("DanskePaymentProviderV1")
    public GenericBasePaymentProviderV3 danskePaymentProviderV1(DanskeBankPropertiesV4 properties,
                                                                MeterRegistry registry,
                                                                Clock clock,
                                                                @Qualifier("OpenBanking") ObjectMapper objectMapper) {
        ProviderIdentification providerIdentification = new ProviderIdentification(PROVIDER_KEY, DISPLAY_NAME, VERSION_1);
        ExternalUserRequestTokenSigner tokenSigner = new ExternalUserRequestTokenSigner(AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        AuthenticationService authenticationService = getAuthenticationService(properties, tokenSigner, clock, true);

        Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> authMeansProvider =
                authenticationMeans -> DanskeAuthMeansBuilderV3
                        .createDefaultAuthenticationMeansForPIS(authenticationMeans, providerIdentification.getDisplayName());
        DateTimeAndUUIDBasedInstructionIdentificationSupplier instructionIdentificationSupplier = new DateTimeAndUUIDBasedInstructionIdentificationSupplier(clock);

        PaymentDataInitiationMapper ukDomesticDataInitiationMapper = new PaymentDataInitiationMapper(instructionIdentificationSupplier,
                new DefaultAmountFormatter(),
                new CamelCaseUkSchemeMapper())
                .withDebtorAccount()
                .validateAfterMapWith(new DanskePaymentRequestValidator());

        TokenScope tokenScope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.PAYMENTS.getGrantScope())
                .authorizationUrlScope(OpenBankingTokenScope.PAYMENTS.getAuthorizationUrlScope())
                .build();
        DefaultPaymentExecutionContextAdapterFactoryV2 executionContextAdapterFactory = new DefaultPaymentExecutionContextAdapterFactoryV2(
                providerIdentification,
                objectMapper,
                authMeansProvider,
                authenticationService,
                new DanskeBankHttpClientFactoryV2(properties, registry, objectMapper),
                ukDomesticDataInitiationMapper,
                null,
                OBRisk1.PaymentContextCodeEnum.PARTYTOPARTY,
                () -> ENDPOINT_VERSION,
                new ExternalPaymentNoB64RequestSigner(objectMapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256),
                clock,
                tokenScope,
                new GenericConsentResponseStatusMapper(),
                new GenericStatusResponseStatusMapper(),
                null,
                null
        );

        return new GenericBasePaymentProviderV3(
                executionContextAdapterFactory.createInitiatePaymentExecutionContextAdapter(),
                null,
                executionContextAdapterFactory.createSubmitPaymentExecutionContextAdapter(),
                null,
                executionContextAdapterFactory.createStatusPaymentExecutionContextAdapter(),
                null,
                providerIdentification,
                getTypedAuthenticationMeansForPIS(),
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME),
                ConsentValidityRules.EMPTY_RULES_SET,
                new UkProviderStateDeserializer(objectMapper));
    }
}