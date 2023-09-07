package com.yolt.providers.openbanking.ais.rbsgroup.ais.v11;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsApp;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsSampleAuthenticationMeansV4;
import com.yolt.providers.openbanking.ais.rbsgroup.common.RbsGroupDataProviderV5;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains fetch data happy flow in all RBS group providers, except Coutts, since it cannot handle pagination on localhost due to port removal.
 * <p>
 * Covered flows:
 * - fetching accounts, balances, transactions, direct debits and standing orders
 * <p>
 * Providers: ALL RBS Group (except Coutts)
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = RbsApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("rbsgroup-v5")
@AutoConfigureWireMock(stubs = "classpath:/stubs/rbsgroup/ob_3.1.6/ais/happy_flow/fetch_data/common", port = 0, httpsPort = 0)
class RbsGroupDataProviderDataFetchHappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();

    private static RestTemplateManagerMock restTemplateManagerMock;
    private static Map<String, BasicAuthenticationMean> authenticationMeans;
    private static String requestTraceId;

    @Autowired
    @Qualifier("NatWestDataProviderV11")
    private RbsGroupDataProviderV5 natwestDataProvider;
    @Autowired
    @Qualifier("NatWestCorporateDataProviderV10")
    private RbsGroupDataProviderV5 natwestCorpoDataProvider;
    @Autowired
    @Qualifier("RoyalBankOfScotlandDataProviderV11")
    private RbsGroupDataProviderV5 rbsDataProvider;
    @Autowired
    @Qualifier("RoyalBankOfScotlandCorporateDataProviderV10")
    private RbsGroupDataProviderV5 rbsCorpoDataProvider;
    @Autowired
    @Qualifier("UlsterBankDataProviderV10")
    private RbsGroupDataProviderV5 ulsterDataProvider;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    private Stream<RbsGroupDataProviderV5> getProviders() {
        return Stream.of(
                natwestDataProvider,
                natwestCorpoDataProvider,
                rbsDataProvider,
                rbsCorpoDataProvider,
                ulsterDataProvider);
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
        authenticationMeans = RbsSampleAuthenticationMeansV4.getRbsSampleAuthenticationMeansForAis();
    }

    @BeforeEach
    void beforeEach() {
        requestTraceId = "12345";
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldFetchData(RbsGroupDataProviderV5 subject) throws Exception {
        // given
        AccessMeans token = new AccessMeans(
                Instant.now(),
                null,
                "test-accounts",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                null,
                null);
        String serializedAccessMeans = objectMapper.writeValueAsString(token);
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, serializedAccessMeans, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        DataProviderResponse dataProviderResponse = subject.fetchData(urlFetchData);

        // then
        // Verify Current Account 1
        assertThat(dataProviderResponse.getAccounts()).hasSize(3);

        ProviderAccountDTO providerAccountDTO = dataProviderResponse.getAccounts().get(0);
        assertThat(providerAccountDTO.getName()).isEqualTo("MOTEST-LBAAC");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo("2.00");
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo("1.00");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);

        // Verify Extended Account Balances
        assertThat(providerAccountDTO.getExtendedAccount().getBalances()).hasSize(2);

        // Verify Stand Order
        assertThat(providerAccountDTO.getStandingOrders()).hasSize(1);
        StandingOrderDTO standingOrderDTO = providerAccountDTO.getStandingOrders().get(0);
        assertThat(standingOrderDTO.getDescription()).isEqualTo("Towbar Club 2 - We Love Towbars");
        assertThat(standingOrderDTO.getFrequency()).isEqualTo(Period.ofDays(1));
        assertThat(standingOrderDTO.getNextPaymentAmount()).isEqualTo("0.56");
        assertThat(standingOrderDTO.getCounterParty().getIdentification()).isEqualTo("80200112345678");

        // Verify Direct Debit
        assertThat(providerAccountDTO.getDirectDebits()).hasSize(1);
        DirectDebitDTO directDebitDTO = providerAccountDTO.getDirectDebits().get(0);
        assertThat(directDebitDTO.getDescription()).isEqualTo("Towbar Club 3 - We Love Towbars");
        assertThat(directDebitDTO.isDirectDebitStatus()).isTrue();
        assertThat(directDebitDTO.getPreviousPaymentAmount()).isEqualTo("0.57");

        // Verify Credit Card
        ProviderAccountDTO creditCardAccount = dataProviderResponse.getAccounts().get(2);
        assertThat(creditCardAccount.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
        validateCreditCardAccount(creditCardAccount);
        validateCreditCardTransactions(creditCardAccount.getTransactions());

        // Verify Extended Account Balances
        assertThat(creditCardAccount.getExtendedAccount().getBalances()).hasSize(2);

        // Verify Current Account 2
        ProviderAccountDTO providerAccountDTO2 = dataProviderResponse.getAccounts().get(1);
        assertThat(providerAccountDTO2.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(providerAccountDTO2.getName()).isEqualTo("MOTEST-LBAAC2");
        assertThat(providerAccountDTO2.getTransactions()).hasSize(17);
        assertThat(providerAccountDTO2.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO2.getAvailableBalance()).isEqualTo("-13.37");
        assertThat(providerAccountDTO2.getCurrentBalance()).isEqualTo("4.20");

        // Verify Extended Account Balances
        assertThat(providerAccountDTO2.getExtendedAccount().getBalances()).hasSize(3);
    }

    private void validateCreditCardAccount(ProviderAccountDTO providerAccountDTO) {
        ExtendedAccountDTO extendedAccount = providerAccountDTO.getExtendedAccount();
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo("2.00");
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo("2.00");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAccountNumber().getIdentification()).isEqualTo("GB11BARC20038015831118");
        assertThat(providerAccountDTO.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(providerAccountDTO.getName()).isEqualTo("Credit Card");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
        assertThat(extendedAccount.getAccountReferences()).contains(
                AccountReferenceDTO.builder().type(AccountReferenceType.IBAN).value("GB11BARC20038015831118").build(),
                AccountReferenceDTO.builder().type(AccountReferenceType.PAN).value("4929712289119828").build()
        );
    }

    private void validateCreditCardTransactions(final List<ProviderTransactionDTO> transactions) {
        ProviderTransactionDTO transaction1 = transactions.get(0);
        assertThat(transaction1.getExternalId()).isEqualTo("1");
        assertThat(transaction1.getAmount()).isEqualTo("0.02");
        assertThat(transaction1.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction1.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transaction1.getCategory()).isEqualTo(YoltCategory.GENERAL);

        ExtendedTransactionDTO extendedTransaction = transaction1.getExtendedTransaction();
        AccountReferenceDTO creditorAccount = extendedTransaction.getCreditorAccount();
        AccountReferenceDTO debtorAccount = extendedTransaction.getDebtorAccount();
        assertThat(creditorAccount.getType()).isEqualTo(AccountReferenceType.MASKED_PAN);
        assertThat(creditorAccount.getValue()).isEqualTo("123456xxxxxx1234");
        assertThat(debtorAccount.getType()).isEqualTo(AccountReferenceType.PAN);
        assertThat(debtorAccount.getValue()).isEqualTo("5409050000000000");

        ProviderTransactionDTO transaction2 = transactions.get(1);
        assertThat(transaction2.getAmount()).isEqualTo("0.01");
        assertThat(transaction2.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction2.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction2.getCategory()).isEqualTo(YoltCategory.GENERAL);

        ExtendedTransactionDTO extendedTransaction2 = transaction2.getExtendedTransaction();
        AccountReferenceDTO creditorAccount2 = extendedTransaction2.getCreditorAccount();
        AccountReferenceDTO debtorAccount2 = extendedTransaction2.getDebtorAccount();
        assertThat(creditorAccount2.getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(creditorAccount2.getValue()).isEqualTo("GB38BARC20035394835536");
        assertThat(debtorAccount2.getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(debtorAccount2.getValue()).isEqualTo("GB38BARC20035394835536");
    }
}
