package com.yolt.providers.openbanking.ais.amexgroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.ais.url.UrlFetchDataRequestBuilder;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProviderV2;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.UsageType;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
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
@SpringBootTest(classes = AmexApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("amexgroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/amexgroup/ais-3.1.8/happy_flow/fetchdata", port = 0, httpsPort = 0)
class AmexGroupDataProviderDataFetchHappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();

    private static RestTemplateManagerMock restTemplateManagerMock;
    private static Map<String, BasicAuthenticationMean> authenticationMeans;
    private static String requestTraceId;

    @Autowired
    @Qualifier("AmexDataProviderV7")
    private GenericBaseDataProviderV2 amexDataProvider;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    private Stream<GenericBaseDataProviderV2> getProviders() {
        return Stream.of(
                amexDataProvider);
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
        authenticationMeans = AmexSampleAuthenticationMeans.getAmexSampleAuthenticationMeans();
    }

    @BeforeEach
    void beforeEach() {
        requestTraceId = "12345";
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldFetchData(GenericBaseDataProviderV2 subject) throws Exception {
        // given
        AccessMeansState<AccessMeans> token = new AccessMeansState<>(new AccessMeans(
                Instant.now(),
                null,
                "user-access-token",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                null,
                null),
                List.of("ReadParty",
                        "ReadAccountsDetail",
                        "ReadBalances",
                        "ReadDirectDebits",
                        "ReadProducts",
                        "ReadStandingOrdersDetail",
                        "ReadTransactionsCredits",
                        "ReadTransactionsDebits",
                        "ReadTransactionsDetail"));
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
        assertThat(dataProviderResponse.getAccounts()).hasSize(1);

        ProviderAccountDTO account = dataProviderResponse.getAccounts().get(0);
        //VerifyAccount
        assertThat(account.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
        assertThat(account.getAvailableBalance()).isEqualTo("300.00");
        assertThat(account.getCurrentBalance()).isEqualTo("300.00");
        assertThat(account.getAccountId()).isEqualTo("22289");
        assertThat(account.getAccountMaskedIdentification()).isEqualTo("XXXX-XXXXXX-21003");
        assertThat(account.getName()).isEqualTo("John Doe");
        assertThat(account.getCurrency()).isEqualTo(CurrencyCode.GBP);
        //Verify ExtendedAccount
        ExtendedAccountDTO extendedAccount = account.getExtendedAccount();
        assertThat(extendedAccount.getResourceId()).isEqualTo("22289");
        assertThat(extendedAccount.getAccountReferences().get(0)).isEqualTo(new AccountReferenceDTO(AccountReferenceType.MASKED_PAN, "XXXX-XXXXXX-21003"));
        assertThat(extendedAccount.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(extendedAccount.getName()).isEqualTo("John Doe");
        assertThat(extendedAccount.getUsage()).isEqualTo(UsageType.CORPORATE);
        assertThat(extendedAccount.getBalances().get(0)).usingRecursiveComparison().ignoringFields("lastChangeDateTime", "referenceDate", "lastCommittedTransaction")
                .isEqualTo(new BalanceDTO(new BalanceAmountDTO(CurrencyCode.GBP, new BigDecimal("300.00")), BalanceType.INFORMATION, null, null, null));

        //Verify Transactions
        List<ProviderTransactionDTO> transactions = account.getTransactions();
        // booked, credit
        ProviderTransactionDTO bookedCreditTransaction = transactions.stream().filter(t -> TransactionStatus.BOOKED.equals(t.getStatus()) && ProviderTransactionType.CREDIT.equals(t.getType())).collect(Collectors.toList()).get(0);
        assertThat(bookedCreditTransaction.getExternalId()).isEqualTo("123");
        assertThat(bookedCreditTransaction.getAmount()).isEqualTo("10.00");
        assertThat(bookedCreditTransaction.getDescription()).isEqualTo("Cash from Aubrey");
        assertThat(bookedCreditTransaction.getMerchant()).isEqualTo("ABCMerchant");
        ExtendedTransactionDTO bookedCreditExtendedTransaction = bookedCreditTransaction.getExtendedTransaction();
        assertThat(bookedCreditExtendedTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(bookedCreditExtendedTransaction.getEntryReference()).isEqualTo("Ref 1");
        assertThat(bookedCreditExtendedTransaction.getTransactionAmount()).isEqualTo(new BalanceAmountDTO(CurrencyCode.GBP, new BigDecimal("10.00")));
        // pending, debit
        ProviderTransactionDTO pendingDebitTransaction = transactions.stream().filter(t -> TransactionStatus.PENDING.equals(t.getStatus()) && ProviderTransactionType.DEBIT.equals(t.getType())).collect(Collectors.toList()).get(0);
        assertThat(pendingDebitTransaction.getExternalId()).isEqualTo("456");
        assertThat(pendingDebitTransaction.getAmount()).isEqualTo("11.00");
        assertThat(pendingDebitTransaction.getDescription()).isEqualTo("Cash for Aubrey");
        assertThat(pendingDebitTransaction.getMerchant()).isEqualTo("ABCMerchant");
        ExtendedTransactionDTO pendingDebitExtendedTransaction = pendingDebitTransaction.getExtendedTransaction();
        assertThat(pendingDebitExtendedTransaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(pendingDebitExtendedTransaction.getEntryReference()).isEqualTo("Ref 2");
        assertThat(pendingDebitExtendedTransaction.getTransactionAmount()).isEqualTo(new BalanceAmountDTO(CurrencyCode.GBP, new BigDecimal("-11.00")));
        // booked, Debit
        ProviderTransactionDTO bookedDebitTransaction = transactions.stream().filter(t -> TransactionStatus.BOOKED.equals(t.getStatus()) && ProviderTransactionType.DEBIT.equals(t.getType())).collect(Collectors.toList()).get(0);
        assertThat(bookedDebitTransaction.getExternalId()).isEqualTo("789");
        assertThat(bookedDebitTransaction.getAmount()).isEqualTo("12.00");
        assertThat(bookedDebitTransaction.getDescription()).isEqualTo("Cash from Stefan");
        assertThat(bookedDebitTransaction.getMerchant()).isEqualTo("ABCMerchant");
        ExtendedTransactionDTO bookedDebitExtendedTransaction = bookedDebitTransaction.getExtendedTransaction();
        assertThat(bookedDebitExtendedTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(bookedDebitExtendedTransaction.getEntryReference()).isEqualTo("Ref 3");
        assertThat(bookedDebitExtendedTransaction.getTransactionAmount()).isEqualTo(new BalanceAmountDTO(CurrencyCode.GBP, new BigDecimal("-12.00")));
//
//        ProviderAccountDTO providerAccountDTO = dataProviderResponse.getAccounts().get(0);
//        assertThat(providerAccountDTO.getName()).isEqualTo("MOTEST-LBAAC");
//        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
//        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo("2.00");
//        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo("1.00");
//        assertThat(providerAccountDTO.getClosed()).isNull();
//        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
//
//        // Verify Extended Account Balances
//        assertThat(providerAccountDTO.getExtendedAccount().getBalances()).hasSize(2);
//
//        // Verify Stand Order
//        assertThat(providerAccountDTO.getStandingOrders()).hasSize(1);
//        StandingOrderDTO standingOrderDTO = providerAccountDTO.getStandingOrders().get(0);
//        assertThat(standingOrderDTO.getDescription()).isEqualTo("Towbar Club 2 - We Love Towbars");
//        assertThat(standingOrderDTO.getFrequency()).isEqualTo(Period.ofDays(1));
//        assertThat(standingOrderDTO.getNextPaymentAmount()).isEqualTo("0.56");
//        assertThat(standingOrderDTO.getCounterParty().getIdentification()).isEqualTo("80200112345678");
//
//        // Verify Direct Debit
//        assertThat(providerAccountDTO.getDirectDebits()).hasSize(1);
//        DirectDebitDTO directDebitDTO = providerAccountDTO.getDirectDebits().get(0);
//        assertThat(directDebitDTO.getDescription()).isEqualTo("Towbar Club 3 - We Love Towbars");
//        assertThat(directDebitDTO.isDirectDebitStatus()).isTrue();
//        assertThat(directDebitDTO.getPreviousPaymentAmount()).isEqualTo("0.57");
//
//        // Verify Credit Card
//        ProviderAccountDTO creditCardAccount = dataProviderResponse.getAccounts().get(2);
//        assertThat(creditCardAccount.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
//        validateCreditCardAccount(creditCardAccount);
//        validateCreditCardTransactions(creditCardAccount.getTransactions());
//
//        // Verify Extended Account Balances
//        assertThat(creditCardAccount.getExtendedAccount().getBalances()).hasSize(2);
//
//        // Verify Current Account 2
//        ProviderAccountDTO providerAccountDTO2 = dataProviderResponse.getAccounts().get(1);
//        assertThat(providerAccountDTO2.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
//        assertThat(providerAccountDTO2.getName()).isEqualTo("MOTEST-LBAAC2");
//        assertThat(providerAccountDTO2.getTransactions()).hasSize(17);
//        assertThat(providerAccountDTO2.getCurrency()).isEqualTo(CurrencyCode.GBP);
//        assertThat(providerAccountDTO2.getAvailableBalance()).isEqualTo("-13.37");
//        assertThat(providerAccountDTO2.getCurrentBalance()).isEqualTo("4.20");
//
//        // Verify Extended Account Balances
//        assertThat(providerAccountDTO2.getExtendedAccount().getBalances()).hasSize(3);
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
