package com.yolt.providers.openbanking.ais.hsbcgroup.marksandspencer.beanconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.openbanking.ais.common.HsmEIdasUtils;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.generic2.domain.LoginInfoState;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.PartyDto;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.oauth2.clientassertion.DefaultClientAssertionProducer;
import com.yolt.providers.openbanking.ais.generic2.oauth2.implementations.PrivateKeyJwtOauth2Client;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.PrivateKeyJwtTokenBodyProducerWithoutScope;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.DefaultConsentPermissions;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.supportedaccounts.DefaultSupportedAccountsSupplier;
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
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.AccessMeansStateMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultAccessMeansStateProvider;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultLoginInfoStateMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.period.DefaultPeriodMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme.CamelcaseSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme.SchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.time.DefaultDateTimeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultDirectDebitMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultStandingOrderMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.DefaultTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.PendingAsNullTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactiontype.DefaultTransactionTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultPartiesRestClient;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.HsbcGroupBaseDataProviderV7;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.auth.HsbcGroupAisTypedAuthMeansSupplier;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.claims.HsbcTokenClaimsProducerV2;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.http.HsbcGroupCertificateIdentityExtractor;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.model.HsbcGroupAccessMeansV2;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.model.mapper.HsbcAccessMeansStateMapper;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.service.HsbcGroupAutoOnboardingServiceV3;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.service.ais.accountmapper.HsbcGroupAccountIdMapper;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.service.ais.accountmapper.HsbcGroupAccountMapperV7;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.service.ais.authenticationservice.HsbcGroupHsmAuthenticationServiceV4;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.service.ais.extendedtransactionmapper.HsbcGroupBalanceMapper;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.service.ais.fetchdataservice.DefaultHsbcGroupFetchDataServiceV8;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.service.ais.fetchdataservice.HsbcFetchDataTimeNarrower;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.service.ais.fetchdataservice.errorhandler.DefaultClosedAndBlockedErrorHandler;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.service.restclient.DefaultHsbcGroupRestClientV5;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.signer.HsbcGroupPaymentRequestSigner;
import com.yolt.providers.openbanking.ais.hsbcgroup.marksandspencer.MarksAndSpencerPropertiesV2;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadConsent1Data;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_13;
import static com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultPermissions.DEFAULT_PERMISSIONS;
import static com.yolt.providers.openbanking.ais.hsbcgroup.common.auth.HsbcGroupAuthMeansBuilderV3.*;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INTERIMAVAILABLE;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INTERIMBOOKED;

@Configuration
public class MarksAndSpencerBeanConfigV2 {

    private static final String IDENTIFIER = "MARKS_AND_SPENCER";
    private static final String DISPLAY_NAME = "Marks and Spencer";
    private static final String ENDPOINT_VERSION = "/v3.1";
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/London");
    private static final String JWS_SIGNING_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
    private final UserRequestTokenSigner userRequestTokenSigner = new ExternalUserRequestTokenSigner(JWS_SIGNING_ALGORITHM);


    private AccountRequestService getAccountRequestServiceV2(ObjectMapper objectMapper,
                                                             AuthenticationService authenticationService,
                                                             List<OBReadConsent1Data.PermissionsEnum> consentPermissions) {
        return new DefaultAccountAccessConsentRequestServiceV2(
                authenticationService,
                getRestClient(objectMapper),
                ENDPOINT_VERSION,
                consentPermissions);
    }

    private DefaultAuthenticationService getAuthenticationService(DefaultProperties properties, Clock clock, boolean isInPisFlow) {
        return new HsbcGroupHsmAuthenticationServiceV4(
                properties.getOAuthAuthorizationUrl(),
                new PrivateKeyJwtOauth2Client(properties.getOAuthTokenUrl(),
                        new PrivateKeyJwtTokenBodyProducerWithoutScope(),
                        new DefaultClientAssertionProducer(
                                new ExternalUserRequestTokenSigner(JWS_SIGNING_ALGORITHM),
                                properties.getOAuthTokenUrl()),
                        isInPisFlow),
                userRequestTokenSigner,
                new HsbcTokenClaimsProducerV2(
                        new DefaultJwtClaimsProducer(
                                DefaultAuthMeans::getClientId,
                                properties.getAudience())),
                clock);
    }

    private DefaultRestClient getRestClient(ObjectMapper objectMapper) {
        return new DefaultHsbcGroupRestClientV5(getPayloadSigner(objectMapper));
    }

    @Bean("MarksAndSpencerDataProviderV13")
    public HsbcGroupBaseDataProviderV7 getMarksAndSpencerDirectDataProviderV13(MarksAndSpencerPropertiesV2 properties,
                                                                               MeterRegistry registry,
                                                                               Clock clock,
                                                                               @Qualifier("OpenBanking") ObjectMapper objectMapper) {
        List<OBReadConsent1Data.PermissionsEnum> consentPermissions = Stream.concat(Stream.of(OBReadConsent1Data.PermissionsEnum.READPARTY),
                DEFAULT_PERMISSIONS.stream()).collect(Collectors.toList());
        ProviderIdentification providerIdentification = new ProviderIdentification(IDENTIFIER, DISPLAY_NAME, VERSION_13);
        Supplier<List<OBBalanceType1Code>> getCurrentBalanceType = () -> Arrays.asList(INTERIMBOOKED);
        Supplier<List<OBBalanceType1Code>> getAvailableBalanceType = () -> Arrays.asList(INTERIMAVAILABLE);
        Function<OBAccount6, String> accountNameFallback = any -> "Marks and Spencer Account";
        Supplier<List<OBBalanceType1Code>> getCurrentBalanceTypeForCreditCard = getCurrentBalanceType;
        Supplier<List<OBBalanceType1Code>> getAvailableBalanceTypeForCreditCard = getAvailableBalanceType;
        AuthenticationService authenticationService = getAuthenticationService(properties, clock, false);
        AccountRequestService accountRequestService = getAccountRequestServiceV2(objectMapper, authenticationService, consentPermissions);
        SchemeMapper schemeMapper = new CamelcaseSchemeMapper();
        Function<String, CurrencyCode> currencyMapper = new DefaultCurrencyMapper();
        BalanceMapper balanceMapper = new HsbcGroupBalanceMapper();
        Function<String, BigDecimal> amountParser = new DefaultAmountParser();
        Function<String, ZonedDateTime> zonedDateTimeMapper = new DefaultDateTimeMapper(ZONE_ID);
        AccountReferenceTypeMapper accountReferenceTypeMapper = new DefaultAccountReferenceTypeMapper();
        DefaultAccountTypeMapper accountTypeMapper = new DefaultAccountTypeMapper();
        AccessMeansStateMapper<AccessMeansState<HsbcGroupAccessMeansV2>> accessMeansStateMapper = new HsbcAccessMeansStateMapper(objectMapper);
        DefaultSupportedSchemeAccountFilter supportedSchemeAccountFilter = new DefaultSupportedSchemeAccountFilter();
        FetchDataServiceV2 fetchDataService =
                new DefaultHsbcGroupFetchDataServiceV8(
                        getRestClient(objectMapper),
                        new DefaultPartiesRestClient(),
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
                        party -> new PartyDto(party.getFullLegalName()),
                        new HsbcGroupAccountMapperV7(
                                getCurrentBalanceType,
                                getAvailableBalanceType,
                                getCurrentBalanceTypeForCreditCard,
                                getAvailableBalanceTypeForCreditCard,
                                currencyMapper,
                                new HsbcGroupAccountIdMapper(
                                        accountTypeMapper,
                                        supportedSchemeAccountFilter
                                ),
                                accountTypeMapper,
                                new CreditCardMapper(),
                                new DefaultAccountNumberMapperV2(schemeMapper),
                                new DefaultAccountNameMapper(accountNameFallback),
                                balanceMapper,
                                new DefaultExtendedAccountMapper(
                                        accountReferenceTypeMapper,
                                        currencyMapper,
                                        new DefaultExtendedBalancesMapper(
                                                new DefaultBalanceAmountMapper(
                                                        currencyMapper,
                                                        new DefaultBalanceMapper()),
                                                new DefaultBalanceTypeMapper(),
                                                ZONE_ID)),
                                supportedSchemeAccountFilter,
                                clock),
                        new DefaultAccountFilter(),
                        new DefaultSupportedAccountsSupplier(),
                        objectMapper,
                        Duration.ofMinutes(60),
                        ENDPOINT_VERSION,
                        clock,
                        new HsbcFetchDataTimeNarrower(clock),
                        new DefaultClosedAndBlockedErrorHandler());
        return new HsbcGroupBaseDataProviderV7(
                fetchDataService,
                accountRequestService,
                authenticationService,
                getHttpClientFactory(properties, registry, objectMapper),
                TokenScope.builder()
                        .grantScope(OpenBankingTokenScope.ACCOUNTS.getGrantScope())
                        .authorizationUrlScope(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope())
                        .build(),
                providerIdentification,
                authenticationMeans -> createAuthenticationMeansForAis(authenticationMeans, DISPLAY_NAME),
                new HsbcGroupAisTypedAuthMeansSupplier(),
                accessMeansStateMapper,
                new DefaultAccessMeansStateProvider(),
                () -> HsmEIdasUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmEIdasUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                () -> {
                    Set<String> keywords = new HashSet<>();
                    keywords.add("Open Banking Auth");
                    return new ConsentValidityRules(keywords);
                },
                new DefaultLoginInfoStateMapper(objectMapper),
                permissions -> new LoginInfoState((List<String>) permissions),
                new DefaultConsentPermissions(consentPermissions),
                properties,
                new HsbcGroupAutoOnboardingServiceV3());
    }

    private DefaultHttpClientFactory getHttpClientFactory(DefaultProperties properties,
                                                          MeterRegistry registry,
                                                          ObjectMapper objectMapper) {
        return new DefaultHttpClientFactory(properties, registry, objectMapper);
    }

    private HsbcGroupPaymentRequestSigner getPayloadSigner(ObjectMapper objectMapper) {
        return new HsbcGroupPaymentRequestSigner(
                objectMapper,
                JWS_SIGNING_ALGORITHM,
                new HsbcGroupCertificateIdentityExtractor());
    }
}

