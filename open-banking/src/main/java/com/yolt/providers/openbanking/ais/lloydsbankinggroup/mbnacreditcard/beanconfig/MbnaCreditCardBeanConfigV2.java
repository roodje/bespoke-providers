package com.yolt.providers.openbanking.ais.lloydsbankinggroup.mbnacreditcard.beanconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.GenericBasePaymentProviderV2;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.FapiCompliantJwtClaimsProducerDecorator;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.oauth2.BasicOauthClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.Oauth2Client;
import com.yolt.providers.openbanking.ais.generic2.oauth2.OneRefreshTokenDecorator;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountrequestservice.AccountRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.FetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.supportedaccounts.DefaultSupportedAccountsSupplier;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountNumberMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountMapper;
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
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentRequestSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.LloydsGroupCommonPaymentProviderFactory;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.auth.LloydsBankingGroupAuthenticationMeansV3;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.consentvalidity.LloydsBankingGroupConsentValidityRulesSupplier;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.http.LloydsBankingGroupHttpClientFactoryV2;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.oauth2.LloydsBasicOauthTokenBodyProducerDecorator;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.service.LloydsBankingGroupAuthenticationServiceV2;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.service.ais.accountmapper.LloydsBankingGroupAccountIdMapper;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.service.ais.extendedtransactionmapper.LloydsBankingGroupExtendedAmountMapper;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.service.ais.fetchdataservice.LloydsBankingGroupFetchDataServiceV7;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.service.claims.LLoydsBankingGroupNonceProvider;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.service.claims.LloydsBankingGroupJwtClaimsProducerV3;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.service.claims.LloydsGroupTokenClaimsProducerV2;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.service.restclient.LloydsBankingGroupRestClientV7;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.mbnacreditcard.MbnaCreditCardPropertiesV2;
import com.yolt.providers.openbanking.ais.lloydsbankinggroup.mbnacreditcard.pec.mapper.MbnaPaymentInitiationAdjuster;
import io.micrometer.core.instrument.MeterRegistry;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Arrays;

import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_11;
import static com.yolt.providers.common.versioning.ProviderVersion.VERSION_6;
import static com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.auth.LloydsBankingGroupAuthenticationMeansV3.*;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INTERIMAVAILABLE;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INTERIMBOOKED;

@Configuration
public class MbnaCreditCardBeanConfigV2 {

    private static final String ACCOUNT_NAME_FALLBACK = "MBNA Credit Card Account";
    private static final String PROVIDER_IDENTIFIER = "MBNA_CREDIT_CARD";
    private static final String PROVIDER_DISPLAY_NAME = "MBNA Credit Card";
    private static final String ENDPOINT_VERSION = "/v3.1";

    private FetchDataService getFetchDataService(MbnaCreditCardPropertiesV2 properties,
                                                 Clock clock,
                                                 final ObjectMapper objectMapper) {
        ZoneId zoneId = ZoneId.of("Europe/London");
        DefaultAmountParser amountParser = new DefaultAmountParser();
        DefaultAccountReferenceTypeMapper accountReferenceTypeMapper = new DefaultAccountReferenceTypeMapper();
        DefaultCurrencyMapper currencyCodeMapper = new DefaultCurrencyMapper();
        DefaultBalanceMapper balanceMapper = new DefaultBalanceMapper();
        DefaultSchemeMapper schemeMapper = new DefaultSchemeMapper();
        DefaultDateTimeMapper zonedDateTimeMapper = new DefaultDateTimeMapper(zoneId);
        DefaultBalanceAmountMapper balanceAmountMapper = new DefaultBalanceAmountMapper(currencyCodeMapper, balanceMapper);
        return new LloydsBankingGroupFetchDataServiceV7(new LloydsBankingGroupRestClientV7(new ExternalPaymentRequestSigner(objectMapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256), objectMapper, properties), properties,
                new DefaultTransactionMapper(
                        new DefaultExtendedTransactionMapper(
                                accountReferenceTypeMapper,
                                new DefaultTransactionStatusMapper(),
                                new DefaultBalanceAmountMapper(currencyCodeMapper, new LloydsBankingGroupExtendedAmountMapper()),
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
                        new LloydsBankingGroupAccountIdMapper(),
                        new DefaultAccountTypeMapper(),
                        new CreditCardMapper(),
                        new AccountNumberMapper(schemeMapper),
                        new DefaultAccountNameMapper(account -> ACCOUNT_NAME_FALLBACK),
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
                Duration.ofMinutes(45),
                ENDPOINT_VERSION,
                clock);
    }

    public AuthenticationService getAuthenticationService(final MbnaCreditCardPropertiesV2 properties,
                                                          final UserRequestTokenSigner tokenSigner,
                                                          final Clock clock,
                                                          boolean isInPisProvider) {
        LLoydsBankingGroupNonceProvider nonceProvider = new LLoydsBankingGroupNonceProvider();
        /** TODO C4PO-11529 As bank decided to return refresh token only once during very first SCA (grant_type=authorization_code) this decorator is needed
         *  after 30.09.2022 action described in {@link OneRefreshTokenDecorator} should be done
         */
        Oauth2Client oauth2Client = new OneRefreshTokenDecorator(new BasicOauthClient<>(properties.getOAuthTokenUrl(),
                defaultAuthMeans -> null,
                new LloydsBasicOauthTokenBodyProducerDecorator(),
                isInPisProvider));
        return new LloydsBankingGroupAuthenticationServiceV2(
                properties.getOAuthAuthorizationUrl(),
                oauth2Client,
                tokenSigner,
                new LloydsGroupTokenClaimsProducerV2(
                        new LloydsBankingGroupJwtClaimsProducerV3(
                                new FapiCompliantJwtClaimsProducerDecorator(
                                        new DefaultJwtClaimsProducer(
                                                DefaultAuthMeans::getClientId,
                                                properties.getAudience()
                                        )
                                ),
                                nonceProvider)),
                nonceProvider,
                clock);
    }

    private AccountRequestService getAccountRequestService(final ObjectMapper mapper,
                                                           final AuthenticationService authenticationService,
                                                           final String endpointVersion,
                                                           final MbnaCreditCardPropertiesV2 properties) {
        return new DefaultAccountAccessConsentRequestService(authenticationService,
                new LloydsBankingGroupRestClientV7(new ExternalPaymentRequestSigner(mapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256), mapper, properties),
                endpointVersion);
    }

    @Bean("MbnaCreditCardDataProviderV6")
    public GenericBaseDataProvider getMbnaCreditCardDataProviderV8(@Qualifier("OpenBanking") final ObjectMapper objectMapper,
                                                                   final MbnaCreditCardPropertiesV2 properties,
                                                                   final MeterRegistry registry,
                                                                   final Clock clock) {
        ExternalUserRequestTokenSigner tokenSigner = new ExternalUserRequestTokenSigner(AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        AuthenticationService authenticationService = getAuthenticationService(properties, tokenSigner, clock, false);
        AccountRequestService accountRequestService = getAccountRequestService(objectMapper, authenticationService, ENDPOINT_VERSION, properties);
        TokenScope scope = TokenScope.builder()
                .grantScope(OpenBankingTokenScope.ACCOUNTS.getGrantScope())
                .authorizationUrlScope(OpenBankingTokenScope.ACCOUNTS.getAuthorizationUrlScope())
                .build();
        return new GenericBaseDataProvider(getFetchDataService(properties, clock, objectMapper),
                accountRequestService,
                authenticationService,
                new LloydsBankingGroupHttpClientFactoryV2(properties, registry, objectMapper),
                scope,
                new ProviderIdentification(PROVIDER_IDENTIFIER, PROVIDER_DISPLAY_NAME, VERSION_6),
                typedAuthMeans -> LloydsBankingGroupAuthenticationMeansV3.createAuthenticationMeansForAis(typedAuthMeans, PROVIDER_IDENTIFIER),
                LloydsBankingGroupAuthenticationMeansV3.getTypedAuthenticationMeansForAIS(),
                new DefaultAccessMeansMapper(objectMapper),
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                new LloydsBankingGroupConsentValidityRulesSupplier()
        );
    }

    @Bean("MbnaPaymentProviderV11")
    public GenericBasePaymentProviderV2 getMbnaPaymentProviderV11(@Qualifier("OpenBanking") final ObjectMapper objectMapper,
                                                                  final MbnaCreditCardPropertiesV2 properties,
                                                                  final MeterRegistry registry,
                                                                  final Clock clock) {
        ProviderIdentification providerIdentification = new ProviderIdentification(PROVIDER_IDENTIFIER, PROVIDER_DISPLAY_NAME, VERSION_11);
        ExternalUserRequestTokenSigner tokenSigner = new ExternalUserRequestTokenSigner(AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        AuthenticationService authenticationService = getAuthenticationService(properties, tokenSigner, clock, true);
        return LloydsGroupCommonPaymentProviderFactory.createPaymentProvider(providerIdentification,
                authenticationService,
                properties,
                registry,
                objectMapper,
                () -> ENDPOINT_VERSION,
                clock,
                new MbnaPaymentInitiationAdjuster());
    }
}
