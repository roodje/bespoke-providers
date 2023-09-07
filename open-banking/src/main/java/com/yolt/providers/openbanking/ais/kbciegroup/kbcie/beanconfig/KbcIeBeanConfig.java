package com.yolt.providers.openbanking.ais.kbciegroup.kbcie.beanconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.generic2.domain.LoginInfoState;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.DefaultHttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.oauth2.Oauth2Client;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.DefaultConsentPermissions;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultConsentWindow;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.frombookingdatetimeformatter.Iso8601FromBookingDateTimeFormatter;
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
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme.DefaultSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.time.DefaultDateTimeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.DefaultTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.PendingAsNullTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactiontype.DefaultTransactionTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultPartiesRestClient;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentRequestSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.kbciegroup.common.KbcIeGroupBaseDataProviderV1;
import com.yolt.providers.openbanking.ais.kbciegroup.common.auth.KbcIeGroupAuthMeansBuilder;
import com.yolt.providers.openbanking.ais.kbciegroup.common.oauth2.client.KbcIeGroupBasicOauthClient;
import com.yolt.providers.openbanking.ais.kbciegroup.common.oauth2.token.KbcIeGroupJwtClaimProducer;
import com.yolt.providers.openbanking.ais.kbciegroup.common.oauth2.token.KbcIeGroupOauthTokenBodyProducer;
import com.yolt.providers.openbanking.ais.kbciegroup.common.oauth2.token.KbcIeGroupUserTokenSigner;
import com.yolt.providers.openbanking.ais.kbciegroup.common.oauth2.token.KbcIeTokenClaimProducer;
import com.yolt.providers.openbanking.ais.kbciegroup.common.service.auth.KbcIeGroupAuthorizationService;
import com.yolt.providers.openbanking.ais.kbciegroup.common.service.autoonboarding.KbcIeGroupAutoOnboardingServiceV1;
import com.yolt.providers.openbanking.ais.kbciegroup.common.service.autoonboarding.KbcIeGroupJwtCreator;
import com.yolt.providers.openbanking.ais.kbciegroup.common.service.fetchdataservice.KbcIeGroupFetchDataService;
import com.yolt.providers.openbanking.ais.kbciegroup.kbcie.KbcIeProperties;
import com.yolt.providers.openbanking.ais.kbciegroup.kbcie.auth.KbcIeAuthMeansBuilder;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadConsent1Data;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBTransaction6;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import io.micrometer.core.instrument.MeterRegistry;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_1;
import static com.yolt.providers.openbanking.ais.kbciegroup.common.auth.KbcIeGroupAuthMeansBuilder.*;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INTERIMAVAILABLE;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INTERIMBOOKED;

@Configuration
public class KbcIeBeanConfig {

    private static final String PROVIDER_KEY = "KBC_IE";
    private static final String DISPLAY_NAME = "KBC IE";
    private static final String ENDPOINT_VERSION = "";
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Dublin");
    private static final SignatureAlgorithm JWS_SIGNING_ALGORITHM = SignatureAlgorithm.SHA256_WITH_RSA;
    private static final List<OBReadConsent1Data.PermissionsEnum> CONSENT_PERMISSIONS = List.of(
            OBReadConsent1Data.PermissionsEnum.READACCOUNTSDETAIL,
            OBReadConsent1Data.PermissionsEnum.READBALANCES,
            OBReadConsent1Data.PermissionsEnum.READPAN,
            OBReadConsent1Data.PermissionsEnum.READDIRECTDEBITS,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSCREDITS,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSDEBITS,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSDETAIL,
            OBReadConsent1Data.PermissionsEnum.READSTANDINGORDERSDETAIL);


    @Bean("KbcIeDataProviderV1")
    public KbcIeGroupBaseDataProviderV1 getKbcIeDataProviderV1(KbcIeProperties properties,
                                                               MeterRegistry meterRegistry,
                                                               Clock clock,
                                                               @Qualifier("OpenBanking") ObjectMapper objectMapper) {
        ProviderIdentification providerIdentification = new ProviderIdentification(PROVIDER_KEY, DISPLAY_NAME, VERSION_1);
        TokenScope scope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.ACCOUNTS.getGrantScope())
                .authorizationUrlScope(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope())
                .build();

        DefaultAuthenticationService authenticationService = getAuthenticationService(properties, clock);
        HttpClientFactory httpClientFactory = new DefaultHttpClientFactory(properties, meterRegistry, objectMapper);
        Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans = KbcIeAuthMeansBuilder::createAuthenticationMeansForAis;

        return new KbcIeGroupBaseDataProviderV1(getFetchDataService(objectMapper, clock, properties),
                getAccountRequestService(objectMapper, authenticationService),
                authenticationService,
                httpClientFactory,
                scope,
                providerIdentification,
                getAuthenticationMeans,
                KbcIeGroupAuthMeansBuilder.getTypedAuthenticationMeansSupplier(),
                new DefaultAccessMeansStateMapper<>(objectMapper),
                AccessMeansState::new,
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME, SIGNING_CERTIFICATE_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                () -> ConsentValidityRules.EMPTY_RULES_SET,
                new DefaultLoginInfoStateMapper<>(objectMapper),
                LoginInfoState::new,
                new DefaultConsentPermissions(CONSENT_PERMISSIONS),
                new KbcIeGroupAutoOnboardingServiceV1(httpClientFactory,
                        getAuthenticationMeans,
                        new KbcIeGroupJwtCreator(),
                        providerIdentification,
                        JWS_SIGNING_ALGORITHM,
                        properties));
    }

    private DefaultAuthenticationService getAuthenticationService(DefaultProperties properties,
                                                                  Clock clock) {

        Oauth2Client oauth2Client = new KbcIeGroupBasicOauthClient<String>(properties.getOAuthTokenUrl(),
                any -> null,
                any -> null,
                any -> null,
                new KbcIeGroupOauthTokenBodyProducer(),
                false);
        UserRequestTokenSigner tokenSigner = new KbcIeGroupUserTokenSigner(JWS_SIGNING_ALGORITHM.getJsonSignatureAlgorithm());
        return new KbcIeGroupAuthorizationService(
                properties.getOAuthAuthorizationUrl(),
                oauth2Client,
                tokenSigner,
                new KbcIeTokenClaimProducer(
                        new KbcIeGroupJwtClaimProducer()),
                clock);
    }

    private FetchDataServiceV2 getFetchDataService(ObjectMapper objectMapper,
                                                   Clock clock,
                                                   DefaultProperties properties) {
        return new KbcIeGroupFetchDataService(
                getRestClient(objectMapper),
                new DefaultPartiesRestClient(),
                properties,
                getTransactionMapper(),
                getAccountMapper(clock),
                new Iso8601FromBookingDateTimeFormatter(),
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
                KbcIeBeanConfig.CONSENT_PERMISSIONS);
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

    private DefaultAccountMapperV3 getAccountMapper(Clock clock) {
        Supplier<List<OBBalanceType1Code>> getCurrentBalanceType = () -> List.of(INTERIMBOOKED);
        Supplier<List<OBBalanceType1Code>> getAvailableBalanceType = () -> List.of(INTERIMAVAILABLE);
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
                new DefaultAccountNameMapper(account -> "Kbc Account"),
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

    private ExternalPaymentRequestSigner getPayloadSigner(ObjectMapper objectMapper) {
        return new ExternalPaymentRequestSigner(
                objectMapper,
                JWS_SIGNING_ALGORITHM.getJsonSignatureAlgorithm());
    }
}

