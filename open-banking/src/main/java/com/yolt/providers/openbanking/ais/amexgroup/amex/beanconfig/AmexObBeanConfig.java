package com.yolt.providers.openbanking.ais.amexgroup.amex.beanconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.openbanking.ais.amexgroup.amex.AmexProperties;
import com.yolt.providers.openbanking.ais.amexgroup.common.AmexGroupBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.amexgroup.common.auth.AmexAuthMeansBuilder;
import com.yolt.providers.openbanking.ais.amexgroup.common.domain.AmexLoginInfoState;
import com.yolt.providers.openbanking.ais.amexgroup.common.http.AmexGroupHttpClientFactory;
import com.yolt.providers.openbanking.ais.amexgroup.common.oauth2.AmexOauthClient;
import com.yolt.providers.openbanking.ais.amexgroup.common.oauth2.AmexOauthTokenBodyProducer;
import com.yolt.providers.openbanking.ais.amexgroup.common.pkce.PKCE;
import com.yolt.providers.openbanking.ais.amexgroup.common.service.AmexGroupAuthenticationService;
import com.yolt.providers.openbanking.ais.amexgroup.common.service.ais.fetchdataservice.AmexGroupFetchDataService;
import com.yolt.providers.openbanking.ais.amexgroup.common.service.ais.fetchdataservice.supportedaccounts.AmexGroupSupportedAccountsSupplier;
import com.yolt.providers.openbanking.ais.amexgroup.common.service.ais.mappers.oauthtoken.AmexGroupLoginInfoStateMapper;
import com.yolt.providers.openbanking.ais.amexgroup.common.service.restclient.AmexGroupRestClient;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.consentvalidity.DefaultConsentValidityRulesSupplier;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.oauth2.tokenbodysupplier.TokenRequestBodyProducer;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.DefaultConsentPermissions;
import com.yolt.providers.openbanking.ais.generic2.service.ais.exceptionhandler.DefaultFetchDataExceptionHandler;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.frombookingdatetimeformatter.DefaultFromBookingDateTimeFormatter;
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
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultAccessMeansStateMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.oauthtoken.DefaultAccessMeansStateProvider;
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
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentNoB64RequestSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadConsent1Data;
import io.micrometer.core.instrument.MeterRegistry;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static com.yolt.providers.openbanking.ais.nationwide.auth.NationwideAuthMeansBuilderV3.*;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INFORMATION;

//TODO C4PO-9807 change name to AmexBeanConfig when old repository will be removed from pom
@Configuration
public class AmexObBeanConfig {

    public static final String PROVIDER_KEY = "AMEX";
    public static final String DISPLAY_NAME = "American Express Cards";
    private static final String ENDPOINT_VERSION = "/v3.1";
    private static final Duration CONSENT_WINDOW_DURATION = Duration.ofMinutes(5);

    private FetchDataServiceV2 getFetchDataService(AmexProperties properties,
                                                   final Clock clock,
                                                   @Qualifier("OpenBanking") final ObjectMapper objectMapper) {
        ZoneId zoneId = ZoneId.of("Europe/London");
        DefaultAmountParser amountParser = new DefaultAmountParser();
        DefaultAccountReferenceTypeMapper accountReferenceTypeMapper = new DefaultAccountReferenceTypeMapper();
        DefaultCurrencyMapper currencyCodeMapper = new DefaultCurrencyMapper();
        DefaultBalanceMapper balanceMapper = new DefaultBalanceMapper();
        DefaultSchemeMapper schemeMapper = new DefaultSchemeMapper();
        DefaultDateTimeMapper zonedDateTimeMapper = new DefaultDateTimeMapper(zoneId);
        DefaultBalanceAmountMapper balanceAmountMapper = new DefaultBalanceAmountMapper(currencyCodeMapper, balanceMapper);
        return new AmexGroupFetchDataService(
                new AmexGroupRestClient(new ExternalPaymentNoB64RequestSigner(objectMapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256)),
                new DefaultPartiesRestClient(),
                properties,
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
                new DefaultPartiesMapper(),
                new DefaultAccountMapperV3(() -> Arrays.asList(INFORMATION), () -> Arrays.asList(INFORMATION),
                        () -> Arrays.asList(INFORMATION), () -> Arrays.asList(INFORMATION),
                        currencyCodeMapper,
                        new DefaultAccountIdMapper(),
                        new DefaultAccountTypeMapper(),
                        new CreditCardMapper(),
                        new DefaultAccountNumberMapperV2(schemeMapper),
                        new DefaultAccountNameMapper(account -> "American Express Cards Account"),
                        balanceMapper,
                        new DefaultExtendedAccountMapper(accountReferenceTypeMapper, currencyCodeMapper,
                                new DefaultExtendedBalancesMapper(
                                        balanceAmountMapper,
                                        new DefaultBalanceTypeMapper(),
                                        zoneId)),
                        new DefaultSupportedSchemeAccountFilter(),
                        clock
                ),
                new DefaultFromBookingDateTimeFormatter(),
                new DefaultAccountFilter(),
                new AmexGroupSupportedAccountsSupplier(),
                CONSENT_WINDOW_DURATION,
                ENDPOINT_VERSION,
                clock,
                new DefaultFetchDataExceptionHandler(),
                new DefaultFetchDataExceptionHandler());
    }


    private AmexGroupAuthenticationService getAuthenticationServiceV2(final AmexProperties properties,
                                                                      final ExternalUserRequestTokenSigner tokenSigner,
                                                                      TokenRequestBodyProducer tokenRequestBodyProducer,
                                                                      final Clock clock,
                                                                      boolean isInPisFlow) {
        return new AmexGroupAuthenticationService(properties.getOAuthAuthorizationUrl(),
                new AmexOauthClient(properties.getOAuthTokenUrl(), DefaultAuthMeans::getClientId,
                        tokenRequestBodyProducer, isInPisFlow),
                tokenSigner,
                new DefaultTokenClaimsProducer(new DefaultJwtClaimsProducer(DefaultAuthMeans::getClientId, properties.getAudience())),
                clock);
    }

    @Bean("AmexDataProviderV7")
    public AmexGroupBaseDataProviderV2 getAmexDataProvider(final AmexProperties properties,
                                                           @Qualifier("OpenBanking") final ObjectMapper objectMapper,
                                                           final MeterRegistry registry,
                                                           final Clock clock) {
        String jwsSigningAlgorithm = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
        ExternalUserRequestTokenSigner tokenSigner = new ExternalUserRequestTokenSigner(jwsSigningAlgorithm);
        AmexGroupAuthenticationService authenticationService = getAuthenticationServiceV2(properties, tokenSigner, new AmexOauthTokenBodyProducer(), clock, false);
        var consentPermissions = List.of(
                OBReadConsent1Data.PermissionsEnum.READACCOUNTSBASIC,
                OBReadConsent1Data.PermissionsEnum.READACCOUNTSDETAIL,
                OBReadConsent1Data.PermissionsEnum.READBALANCES,
                OBReadConsent1Data.PermissionsEnum.READPRODUCTS,
                OBReadConsent1Data.PermissionsEnum.READSTATEMENTSBASIC,
                OBReadConsent1Data.PermissionsEnum.READSTATEMENTSDETAIL,
                OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSBASIC,
                OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSCREDITS,
                OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSDEBITS,
                OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSDETAIL);
        AccountRequestService accountRequestService = new DefaultAccountAccessConsentRequestServiceV2(
                authenticationService,
                new AmexGroupRestClient(
                        new ExternalPaymentNoB64RequestSigner(objectMapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256)),
                ENDPOINT_VERSION,
                consentPermissions);
        TokenScope tokenScope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.ACCOUNTS.getGrantScope())
                .authorizationUrlScope(OpenBankingTokenScope.ACCOUNTS.getGrantScope())
                .build();
        return new AmexGroupBaseDataProviderV2(
                getFetchDataService(properties, clock, objectMapper),
                accountRequestService,
                authenticationService,
                new AmexGroupHttpClientFactory(properties, registry, objectMapper),
                tokenScope,
                new ProviderIdentification(PROVIDER_KEY, DISPLAY_NAME, ProviderVersion.VERSION_7),
                AmexAuthMeansBuilder.createAuthenticationMeansFunction(),
                AmexAuthMeansBuilder.getTypedAuthenticationMeans(),
                new DefaultAccessMeansStateMapper(objectMapper),
                new DefaultAccessMeansStateProvider(),
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                new DefaultConsentValidityRulesSupplier(),
                new AmexGroupLoginInfoStateMapper(objectMapper),
                AmexLoginInfoState::new,
                new DefaultConsentPermissions(consentPermissions),
                new PKCE());
    }
}
