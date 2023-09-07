package com.yolt.providers.openbanking.ais.generic2.configuration.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper.DefaultUkRemittanceInformationMapper;
import com.yolt.providers.openbanking.ais.common.v4.ukpaymentmapper.WithoutDebtorUkPaymentMapper;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme.DefaultUkSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.accountaccessconsentrequestservice.DefaultAccountAccessConsentRequestService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultAccountFilter;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultConsentWindow;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataService;
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
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.period.DefaultPeriodMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.scheme.DefaultSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.time.DefaultDateTimeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultDirectDebitMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultStandingOrderMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactionstatus.DefaultTransactionStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transactiontype.DefaultTransactionTypeMapper;
import com.yolt.providers.openbanking.ais.generic2.service.pis.paymentservice.DefaultUkDomesticPaymentService;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalPaymentRequestSigner;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Arrays;

import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.*;

@Configuration
public class GenericTestConfigurationV1 {

    @Bean
    @Qualifier("FetchDataServiceV6")
    public DefaultFetchDataService getFetchDataServiceV6(final DefaultProperties properties,
                                                         final Clock clock,
                                                         final @Qualifier("OpenBanking") ObjectMapper mapper) {
        ZoneId zoneId = ZoneId.of("Europe/London");
        DefaultAmountParser amountParser = new DefaultAmountParser();
        DefaultAccountReferenceTypeMapper accountReferenceTypeMapper = new DefaultAccountReferenceTypeMapper();
        DefaultCurrencyMapper currencyCodeMapper = new DefaultCurrencyMapper();
        DefaultBalanceMapper balanceMapper = new DefaultBalanceMapper();
        DefaultSchemeMapper schemeMapper = new DefaultSchemeMapper();
        DefaultDateTimeMapper zonedDateTimeMapper = new DefaultDateTimeMapper(zoneId);
        return new DefaultFetchDataService(new DefaultRestClient(new ExternalPaymentRequestSigner(mapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256)), properties,
                new DefaultTransactionMapper(
                        new DefaultExtendedTransactionMapper(accountReferenceTypeMapper,
                                new DefaultTransactionStatusMapper(),
                                new DefaultBalanceAmountMapper(currencyCodeMapper, balanceMapper),
                                false,
                                zoneId),
                        zonedDateTimeMapper,
                        new DefaultTransactionStatusMapper(),
                        amountParser,
                        new DefaultTransactionTypeMapper()),
                new DefaultDirectDebitMapper(zoneId, amountParser),
                new DefaultStandingOrderMapper(new DefaultPeriodMapper(), amountParser, schemeMapper, zonedDateTimeMapper),
                new DefaultAccountMapper(() -> Arrays.asList(INTERIMBOOKED), () -> Arrays.asList(INTERIMAVAILABLE), () -> Arrays.asList(OPENINGCLEARED), () -> Arrays.asList(FORWARDAVAILABLE),
                        currencyCodeMapper,
                        new DefaultAccountIdMapper(),
                        new DefaultAccountTypeMapper(),
                        new CreditCardMapper(),
                        new AccountNumberMapper(schemeMapper),
                        new DefaultAccountNameMapper(account -> "Test implementation Open Banking Account"),
                        balanceMapper,
                        new DefaultExtendedAccountMapper(accountReferenceTypeMapper, currencyCodeMapper,
                                new DefaultExtendedBalancesMapper(
                                        new DefaultBalanceAmountMapper(currencyCodeMapper, balanceMapper),
                                        new DefaultBalanceTypeMapper(),
                                        zoneId)),
                        new DefaultSupportedSchemeAccountFilter(),
                        clock),
                new DefaultAccountFilter(),
                new DefaultSupportedAccountsSupplier(),
                DefaultConsentWindow.DURATION,
                "",
                clock);
    }

    @Bean
    @Qualifier("FetchDataServiceV7")
    public DefaultFetchDataService getFetchDataServiceV7(final DefaultProperties properties,
                                                         final Clock clock,
                                                         final @Qualifier("OpenBanking") ObjectMapper mapper) {
        ZoneId zoneId = ZoneId.of("Europe/London");
        DefaultAmountParser amountParser = new DefaultAmountParser();
        DefaultAccountReferenceTypeMapper accountReferenceTypeMapper = new DefaultAccountReferenceTypeMapper();
        DefaultCurrencyMapper currencyCodeMapper = new DefaultCurrencyMapper();
        DefaultBalanceMapper balanceMapper = new DefaultBalanceMapper();
        DefaultSchemeMapper schemeMapper = new DefaultSchemeMapper();
        DefaultDateTimeMapper zonedDateTimeMapper = new DefaultDateTimeMapper(zoneId);
        return new DefaultFetchDataService(new DefaultRestClient(new ExternalPaymentRequestSigner(mapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256)), properties,
                new DefaultTransactionMapper(
                        new DefaultExtendedTransactionMapper(accountReferenceTypeMapper,
                                new DefaultTransactionStatusMapper(),
                                new DefaultBalanceAmountMapper(currencyCodeMapper, balanceMapper),
                                false,
                                zoneId),
                        zonedDateTimeMapper,
                        new DefaultTransactionStatusMapper(),
                        amountParser,
                        new DefaultTransactionTypeMapper()),
                new DefaultDirectDebitMapper(zoneId, amountParser),
                new DefaultStandingOrderMapper(new DefaultPeriodMapper(), amountParser, schemeMapper, zonedDateTimeMapper),
                new DefaultAccountMapperV2(() -> Arrays.asList(INTERIMBOOKED), () -> Arrays.asList(INTERIMAVAILABLE), () -> Arrays.asList(OPENINGCLEARED), () -> Arrays.asList(FORWARDAVAILABLE),
                        currencyCodeMapper,
                        new DefaultAccountIdMapper(),
                        new DefaultAccountTypeMapper(),
                        new CreditCardMapper(),
                        new AccountNumberMapper(schemeMapper),
                        new DefaultAccountNameMapper(account -> "Test implementation Open Banking Account"),
                        balanceMapper,
                        new DefaultExtendedAccountMapper(accountReferenceTypeMapper, currencyCodeMapper,
                                new DefaultExtendedBalancesMapper(
                                        new DefaultBalanceAmountMapper(currencyCodeMapper, balanceMapper),
                                        new DefaultBalanceTypeMapper(),
                                        zoneId)),
                        new DefaultSupportedSchemeAccountFilter(),
                        clock),
                new DefaultAccountFilter(),
                new DefaultSupportedAccountsSupplier(),
                DefaultConsentWindow.DURATION,
                "",
                clock);
    }

    @Bean
    public DefaultAccountAccessConsentRequestService getAccountRequestServiceV4(@Qualifier("OpenBanking") ObjectMapper mapper,
                                                                                AuthenticationService authenticationService) {
        return new DefaultAccountAccessConsentRequestService(authenticationService,
                new DefaultRestClient(new ExternalPaymentRequestSigner(mapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256)), "");
    }

    @Bean
    public DefaultUkDomesticPaymentService getUkPaymentService(final AuthenticationService authenticationService,
                                                               final Clock clock,
                                                               final @Qualifier("OpenBanking") ObjectMapper mapper) {
        return new DefaultUkDomesticPaymentService(authenticationService,
                new DefaultRestClient(new ExternalPaymentRequestSigner(mapper, AlgorithmIdentifiers.RSA_PSS_USING_SHA256)), mapper,
                new WithoutDebtorUkPaymentMapper(new DefaultUkRemittanceInformationMapper(), new DefaultUkSchemeMapper(), clock), "");
    }
}
