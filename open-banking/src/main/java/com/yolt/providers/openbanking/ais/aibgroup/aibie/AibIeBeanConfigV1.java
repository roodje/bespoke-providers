package com.yolt.providers.openbanking.ais.aibgroup.aibie;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.domain.consenttesting.ConsentValidityRules;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.openbanking.ais.aibgroup.aibie.auth.AibIeAuthMeansSupplier;
import com.yolt.providers.openbanking.ais.aibgroup.common.auth.AibGroupAuthMeansBuilderV3;
import com.yolt.providers.openbanking.ais.aibgroup.common.http.AibGroupHttpClientFactoryV2;
import com.yolt.providers.openbanking.ais.aibgroup.common.oauth2.AibGroupOAuth2ClientV2;
import com.yolt.providers.openbanking.ais.aibgroup.common.service.ais.accountmapper.AibGroupAccountMapperV3;
import com.yolt.providers.openbanking.ais.common.HsmUtils;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.FapiCompliantJwtClaimsProducerDecorator;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestServiceV2;
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
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentRequestSigner;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadConsent1Data;
import io.micrometer.core.instrument.MeterRegistry;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.yolt.providers.openbanking.ais.aibgroup.common.auth.AibGroupAuthMeansBuilderV3.*;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.INTERIMAVAILABLE;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.OPENINGAVAILABLE;

@Configuration
public class AibIeBeanConfigV1 {

    public static final String PROVIDER_KEY = "AIB_IE";
    public static final String DISPLAY_NAME = "AIB IE";
    private static final String ENDPOINT_VERSION = "/v3.1";
    private static final List<OBReadConsent1Data.PermissionsEnum> PERMISSIONS = Arrays.asList(
            OBReadConsent1Data.PermissionsEnum.READACCOUNTSDETAIL,
            OBReadConsent1Data.PermissionsEnum.READBALANCES,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSCREDITS,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSDEBITS,
            OBReadConsent1Data.PermissionsEnum.READTRANSACTIONSDETAIL);

    private FetchDataService getFetchDataService(AibIePropertiesV1 properties,
                                                 final Clock clock,
                                                 final ObjectMapper objectMapper) {
        ZoneId zoneId = ZoneId.of("Europe/Dublin");
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
                        new DefaultAccountNameMapper(account -> "AIB IE Account"),
                        new DefaultExtendedAccountMapper(accountReferenceTypeMapper,
                                currencyCodeMapper,
                                new DefaultExtendedBalancesMapper(
                                        balanceAmountMapper,
                                        new DefaultBalanceTypeMapper(),
                                        zoneId)),
                        balanceMapper,
                        new DefaultSupportedSchemeAccountFilter(),
                        clock),
                new DefaultAccountFilter(),
                new DefaultSupportedAccountsSupplier(),
                DefaultConsentWindow.DURATION,
                ENDPOINT_VERSION,
                clock);
    }

    private AuthenticationService getAuthenticationServiceV1(final AibIePropertiesV1 properties,
                                                             final ExternalUserRequestTokenSigner tokenSigner,
                                                             final Clock clock,
                                                             boolean isInPisFlow) {
        return new DefaultAuthenticationService(properties.getOAuthAuthorizationUrl(),
                new AibGroupOAuth2ClientV2(properties, isInPisFlow),
                tokenSigner,
                new DefaultTokenClaimsProducer(new FapiCompliantJwtClaimsProducerDecorator(new DefaultJwtClaimsProducer(DefaultAuthMeans::getClientId,
                        properties.getAudience()))),
                clock);
    }

    private AccountRequestService getAccountRequestService(final ObjectMapper mapper,
                                                           final AuthenticationService authenticationService,
                                                           final String endpointVersion) {
        return new DefaultAccountAccessConsentRequestServiceV2(
                authenticationService,
                new DefaultRestClient(new ExternalPaymentRequestSigner(mapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256)),
                endpointVersion,
                PERMISSIONS
        );
    }

    @Bean("AibIeDataProviderV1")
    public GenericBaseDataProvider getAibIeDataProviderV1(final AibIePropertiesV1 properties,
                                                          @Qualifier("OpenBanking") final ObjectMapper objectMapper,
                                                          final MeterRegistry registry,
                                                          final Clock clock) {
        String jwsSigningAlgorithm = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
        ExternalUserRequestTokenSigner tokenSigner = new ExternalUserRequestTokenSigner(jwsSigningAlgorithm);
        AuthenticationService authenticationService = getAuthenticationServiceV1(properties, tokenSigner, clock, false);
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
                new ProviderIdentification(PROVIDER_KEY, DISPLAY_NAME, ProviderVersion.VERSION_1),
                defaultAuthMeans -> AibGroupAuthMeansBuilderV3.createAuthenticationMeansForAis(defaultAuthMeans, PROVIDER_KEY),
                new AibIeAuthMeansSupplier(),
                new DefaultAccessMeansMapper(objectMapper),
                () -> HsmUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME),
                () -> HsmUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME),
                () -> new ConsentValidityRules(Collections.singleton("AIB Internet Banking"))
        );
    }
}
