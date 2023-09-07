package com.yolt.providers.openbanking.ais.permanenttsbgroup.permanenttsb.beanconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.types.StringType;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.generic2.domain.LoginInfoState;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.DefaultConsentPermissions;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultConsentWindow;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.supportedaccounts.DefaultSupportedAccountsSupplier;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.*;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.schemesupport.DefaultSupportedSchemeAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.accountrefferencetypemapper.DefaultAccountReferenceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.amount.DefaultAmountParser;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.DefaultBalanceMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balanceamount.BalanceAmountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balanceamount.DefaultBalanceAmountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balancetype.DefaultBalanceTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.creditcard.CreditCardMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.currency.DefaultCurrencyMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedaccount.DefaultExtendedAccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedbalance.DefaultExtendedBalancesMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.extendedtransaction.DefaultExtendedTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultAccessMeansStateMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultLoginInfoStateMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.parties.DefaultPartiesMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.period.DefaultPeriodMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme.DefaultSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.time.DefaultDateTimeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultDirectDebitMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultStandingOrderMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.DefaultTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.PendingAsNullTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactiontype.DefaultTransactionTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultPartiesRestClient;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentRequestSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.common.PermanentTsbGroupBaseDataProviderV1;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.common.http.PermanentTsbGroupHttpClientFactory;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.common.oauth2.PermanentTsbGroupClientAssertionProducer;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.common.oauth2.PermanentTsbGroupClientSecretBasicOAuth2Client;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.common.oauth2.PermanentTsbGroupTokenBodyProducer;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.common.oauth2.PermanentTsbGroupTppSignatureCertificateHeaderProducer;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.common.service.PermanentTsbFetchDataServiceV1;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.common.service.PermanentTsbGroupAutoOnboardingServiceV1;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.common.service.frombookingdatetimeformatter.PermanentTsbGroupFromBookingDateTimeFormatter;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.permanenttsb.PermanentTsbProperties;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.permanenttsb.auth.PermanentTsbAuthMeansBuilder;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import io.micrometer.core.instrument.MeterRegistry;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.yolt.providers.common.domain.authenticationmeans.RenderingType.ONE_LINE_STRING;
import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;
import static com.yolt.providers.openbanking.ais.permanenttsbgroup.common.auth.PermanentTsbGroupAuthMeansBuilder.*;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INFORMATION;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INTERIMBOOKED;

@Configuration
public class PermanentTsbBeanConfig {

    private static final String PROVIDER_KEY = "PERMANENT_TSB";
    private static final String DISPLAY_NAME = "Permanent TSB";
    private static final String ENDPOINT_VERSION = "/v3.1";
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Dublin");
    private static final String JWS_SIGNING_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
    private static final List<OBReadConsent1Data.PermissionsEnum> CONSENT_PERMISSIONS = List.of(
            OBReadConsent1Data.PermissionsEnum.READACCOUNTSDETAIL,
            OBReadConsent1Data.PermissionsEnum.READBALANCES,
            OBReadConsent1Data.PermissionsEnum.READPAN,
            OBReadConsent1Data.PermissionsEnum.READDIRECTDEBITS,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSCREDITS,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSDEBITS,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSDETAIL,
            OBReadConsent1Data.PermissionsEnum.READSTANDINGORDERSDETAIL,
            OBReadConsent1Data.PermissionsEnum.READACCOUNTSBASIC,
            OBReadConsent1Data.PermissionsEnum.READSCHEDULEDPAYMENTSBASIC,
            OBReadConsent1Data.PermissionsEnum.READSCHEDULEDPAYMENTSDETAIL,
            OBReadConsent1Data.PermissionsEnum.READSTANDINGORDERSBASIC,
            OBReadConsent1Data.PermissionsEnum.READSTATEMENTSBASIC,
            OBReadConsent1Data.PermissionsEnum.READSTATEMENTSDETAIL,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSBASIC
    );

    private final UserRequestTokenSigner userRequestTokenSigner = new ExternalUserRequestTokenSigner(JWS_SIGNING_ALGORITHM);

    @Bean("PermanentTsbDataProviderV1")
    public PermanentTsbGroupBaseDataProviderV1 getPermanentTsbDataProviderV1(PermanentTsbProperties properties,
                                                                             MeterRegistry meterRegistry,
                                                                             Clock clock,
                                                                             @Qualifier("OpenBanking") ObjectMapper objectMapper) {
        ProviderIdentification providerIdentification = new ProviderIdentification(PROVIDER_KEY, DISPLAY_NAME, VERSION_1);
        TokenScope scope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.ACCOUNTS.getGrantScope())
                .authorizationUrlScope(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope())
                .build();

        AuthenticationService authenticationService = getAuthenticationService(properties, clock, scope);
        HttpClientFactory httpClientFactory = new PermanentTsbGroupHttpClientFactory(properties, meterRegistry, objectMapper);
        Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans = PermanentTsbAuthMeansBuilder::createAuthenticationMeansForAis;

        return new PermanentTsbGroupBaseDataProviderV1(getFetchDataService(objectMapper, clock, properties),
                getAccountRequestService(objectMapper, authenticationService),
                authenticationService,
                httpClientFactory,
                scope,
                providerIdentification,
                getAuthenticationMeans,
                getTypedAuthenticationMeansSupplier(),
                new DefaultAccessMeansStateMapper<>(objectMapper),
                AccessMeansState::new,
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME, SIGNING_CERTIFICATE_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                () -> ConsentValidityRules.EMPTY_RULES_SET,
                new DefaultLoginInfoStateMapper<>(objectMapper),
                LoginInfoState::new,
                new DefaultConsentPermissions(CONSENT_PERMISSIONS),
                new PermanentTsbGroupAutoOnboardingServiceV1(properties,
                        httpClientFactory,
                        objectMapper,
                        getAuthenticationMeans,
                        providerIdentification,
                        new PermanentTsbGroupTppSignatureCertificateHeaderProducer()));
    }

    private AuthenticationService getAuthenticationService(DefaultProperties properties,
                                                           Clock clock,
                                                           TokenScope scope) {
        return new DefaultAuthenticationService(
                properties.getOAuthAuthorizationUrl(),
                new PermanentTsbGroupClientSecretBasicOAuth2Client(new PermanentTsbGroupTokenBodyProducer(scope),
                        new PermanentTsbGroupClientAssertionProducer(userRequestTokenSigner, properties.getOAuthTokenUrl()),
                        new PermanentTsbGroupTppSignatureCertificateHeaderProducer(),
                        properties,
                        false),
                userRequestTokenSigner,
                new DefaultTokenClaimsProducer(
                        new DefaultJwtClaimsProducer(
                                DefaultAuthMeans::getClientId,
                                properties.getAudience())),
                clock);
    }

    private FetchDataServiceV2 getFetchDataService(ObjectMapper objectMapper,
                                                   Clock clock,
                                                   PermanentTsbProperties properties) {
        return new PermanentTsbFetchDataServiceV1(
                getRestClient(objectMapper),
                new DefaultPartiesRestClient(),
                properties,
                getTransactionMapper(),
                getDirectDebitMapper(),
                getStandingOrderMapper(),
                new DefaultPartiesMapper(),
                getAccountMapper(clock),
                new PermanentTsbGroupFromBookingDateTimeFormatter(),
                new DefaultAccountFilter(),
                new DefaultSupportedAccountsSupplier(),
                DefaultConsentWindow.DURATION,
                ENDPOINT_VERSION,
                clock);
    }

    private AccountRequestService getAccountRequestService(ObjectMapper objectMapper,
                                                           AuthenticationService authenticationService) {
        return new DefaultAccountAccessConsentRequestServiceV2(
                authenticationService,
                getRestClient(objectMapper),
                ENDPOINT_VERSION,
                PermanentTsbBeanConfig.CONSENT_PERMISSIONS);
    }

    private Function<OBTransaction6, ProviderTransactionDTO> getTransactionMapper() {
        DefaultDateTimeMapper zonedDateTimeMapper = new DefaultDateTimeMapper(ZONE_ID);
        return new DefaultTransactionMapper(
                new DefaultExtendedTransactionMapper(
                        new DefaultAccountReferenceTypeMapper(),
                        new DefaultTransactionStatusMapper(),
                        getBalanceAmountMapper(),
                        false,
                        ZONE_ID),
                zonedDateTimeMapper,
                new PendingAsNullTransactionStatusMapper(),
                new DefaultAmountParser(),
                new DefaultTransactionTypeMapper());
    }

    private Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> getDirectDebitMapper() {
        return new DefaultDirectDebitMapper(ZONE_ID, new DefaultAmountParser());
    }

    private Function<OBStandingOrder6, StandingOrderDTO> getStandingOrderMapper() {
        return new DefaultStandingOrderMapper(new DefaultPeriodMapper(),
                new DefaultAmountParser(),
                new DefaultSchemeMapper(),
                new DefaultDateTimeMapper(ZONE_ID));
    }

    private DefaultAccountMapperV3 getAccountMapper(Clock clock) {
        Supplier<List<OBBalanceType1Code>> getCurrentBalanceType = () -> List.of(INTERIMBOOKED);
        Supplier<List<OBBalanceType1Code>> getAvailableBalanceType = () -> List.of(INFORMATION);
        Supplier<List<OBBalanceType1Code>> getCurrentBalanceTypeForCreditCard = getCurrentBalanceType;
        Supplier<List<OBBalanceType1Code>> getAvailableBalanceTypeForCreditCard = getAvailableBalanceType;

        return new DefaultAccountMapperV3(
                getCurrentBalanceType,
                getAvailableBalanceType,
                getCurrentBalanceTypeForCreditCard,
                getAvailableBalanceTypeForCreditCard,
                new DefaultCurrencyMapper(),
                new DefaultAccountIdMapper(),
                new DefaultAccountTypeMapper(),
                new CreditCardMapper(),
                new DefaultAccountNumberMapperV2(new DefaultSchemeMapper()),
                new DefaultAccountNameMapper(account -> "Permanent TSB Account"),
                new DefaultBalanceMapper(),
                new DefaultExtendedAccountMapper(new DefaultAccountReferenceTypeMapper(),
                        new DefaultCurrencyMapper(),
                        new DefaultExtendedBalancesMapper(
                                getBalanceAmountMapper(),
                                new DefaultBalanceTypeMapper(),
                                ZONE_ID)),
                new DefaultSupportedSchemeAccountFilter(),
                clock);
    }

    private BalanceAmountMapper getBalanceAmountMapper() {
        return new DefaultBalanceAmountMapper(new DefaultCurrencyMapper(), new DefaultBalanceMapper());
    }

    private DefaultRestClient getRestClient(ObjectMapper objectMapper) {
        return new DefaultRestClient(getPayloadSigner(objectMapper));
    }

    private Supplier<Map<String, TypedAuthenticationMeans>> getTypedAuthenticationMeansSupplier() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(INSTITUTION_ID_NAME, TypedAuthenticationMeans.INSTITUTION_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        typedAuthenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(SIGNING_KEY_HEADER_ID_NAME, TypedAuthenticationMeans.SIGNING_KEY_ID_STRING);
        typedAuthenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        typedAuthenticationMeans.put(SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        typedAuthenticationMeans.put(ORGANISATION_NAME_NAME, getCustomizedTypedAuthenticationMean("Organisation name"));
        typedAuthenticationMeans.put(APPLICATION_NAME_NAME, getCustomizedTypedAuthenticationMean("Application name"));
        typedAuthenticationMeans.put(BUSINESS_CONTACT_NAME_NAME, getCustomizedTypedAuthenticationMean("Business contact name"));
        typedAuthenticationMeans.put(BUSINESS_CONTACT_EMAIL_NAME, getCustomizedTypedAuthenticationMean("Business contact e-mail"));
        typedAuthenticationMeans.put(BUSINESS_CONTACT_PHONE_NAME, getCustomizedTypedAuthenticationMean("Business contact phone number"));
        typedAuthenticationMeans.put(TECHNICAL_CONTACT_NAME_NAME, getCustomizedTypedAuthenticationMean("Technical contact name"));
        typedAuthenticationMeans.put(TECHNICAL_CONTACT_EMAIL_NAME, getCustomizedTypedAuthenticationMean("Technical contact e-mail"));
        typedAuthenticationMeans.put(TECHNICAL_CONTACT_PHONE_NAME, getCustomizedTypedAuthenticationMean("Technical contact phone number"));
        return () -> typedAuthenticationMeans;
    }

    private TypedAuthenticationMeans getCustomizedTypedAuthenticationMean(String displayName) {
        return new TypedAuthenticationMeans(displayName, StringType.getInstance(), ONE_LINE_STRING);
    }

    private ExternalPaymentRequestSigner getPayloadSigner(ObjectMapper objectMapper) {
        return new ExternalPaymentRequestSigner(
                objectMapper,
                JWS_SIGNING_ALGORITHM);
    }
}

