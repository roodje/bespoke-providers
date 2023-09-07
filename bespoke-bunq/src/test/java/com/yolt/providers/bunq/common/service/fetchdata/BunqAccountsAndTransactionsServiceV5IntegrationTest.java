package com.yolt.providers.bunq.common.service.fetchdata;

import com.bunq.sdk.security.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.bunq.RestTemplateManagerMock;
import com.yolt.providers.bunq.TestApp;
import com.yolt.providers.bunq.common.auth.BunqApiContext;
import com.yolt.providers.bunq.common.configuration.BunqProperties;
import com.yolt.providers.bunq.common.http.BunqHttpClientFactory;
import com.yolt.providers.bunq.common.http.BunqHttpClientV5;
import com.yolt.providers.bunq.common.http.BunqHttpHeaderProducer;
import com.yolt.providers.bunq.common.http.BunqHttpServiceV5;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import io.micrometer.core.instrument.MeterRegistry;
import nl.ing.lovebird.extendeddata.account.*;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionType;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(httpsPort = 0, port = 0)
public class BunqAccountsAndTransactionsServiceV5IntegrationTest {

    private static final ZoneId AMSTERDAM_TIMEZONE = ZoneId.of("Europe/Amsterdam");
    public static final Instant TRANSACTIONS_FETCH_START_TIME = Instant.parse("2017-02-03T10:37:30.00Z");

    @Autowired
    private BunqAccountsAndTransactionsServiceV5 accountsAndTransactionsService;

    @Autowired
    private BunqProperties properties;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MeterRegistry meterRegistry;

    private RestTemplateManagerMock restTemplateManagerMock;
    private BunqHttpServiceV5 httpService;

    @BeforeEach
    public void beforeEach() {
        restTemplateManagerMock = new RestTemplateManagerMock(externalRestTemplateBuilderFactory);
        BunqHttpClientFactory httpClientFactory = new BunqHttpClientFactory(meterRegistry, objectMapper, properties, new BunqHttpHeaderProducer(objectMapper));
        BunqHttpClientV5 httpClient = httpClientFactory.createHttpClient(restTemplateManagerMock, "BUNQ");
        httpService = new BunqHttpServiceV5(properties, httpClient);
    }

    @Test
    public void shouldConvertAllTransactionsWhenRetrievingAllTransactionsForAnAccount() throws TokenInvalidException {
        //given
        BunqApiContext context = new BunqApiContext("1", "serverToken", SecurityUtils.generateKeyPair(), "oauthToken", "sessionToken", 1L);

        // when
        List<ProviderTransactionDTO> convertedTransactions = accountsAndTransactionsService.fetchTransactionsForAccount(httpService, context, "1", TRANSACTIONS_FETCH_START_TIME);

        // then
        assertThat(convertedTransactions).hasSize(4);
        ProviderTransactionDTO firstTransaction = convertedTransactions.get(0);
        firstTransaction.validate(); // Should not fail the test if it is constructed properly
        assertThat(firstTransaction.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(firstTransaction.getAmount()).isEqualTo("20.00");
        assertThat(firstTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(firstTransaction.getMerchant()).isNull();
        assertThat(firstTransaction.getDateTime()).isEqualTo("2018-09-04T13:49:03.006898+02:00[Europe/Amsterdam]");
    }

    @Test
    public void shouldConvertAllAccountsWhenRetrievingAllUserAccounts() throws TokenInvalidException, ProviderFetchDataException {
        // given
        BunqApiContext context = new BunqApiContext("1", "serverToken", SecurityUtils.generateKeyPair(), "oauthToken", "sessionToken", 1L);

        // when
        List<ProviderAccountDTO> convertedAccounts = accountsAndTransactionsService.fetchAccountsAndTransactionsForUser(context, httpService, TRANSACTIONS_FETCH_START_TIME);

        // then
        assertThat(convertedAccounts).hasSize(1);
        ProviderAccountDTO firstAccount = convertedAccounts.get(0);
        firstAccount.validate();
        assertThat(firstAccount.getTransactions()).hasSize(4);
        assertThat(firstAccount.getName()).isEqualTo("bunq account");
        assertThat(firstAccount.getAccountNumber()).isNotNull();
        assertThat(firstAccount.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(firstAccount.getAccountNumber().getIdentification()).isEqualTo("NL94BUNQ9900054180");
        assertThat(firstAccount.getAccountNumber().getHolderName()).isEqualTo("Joeri Lawrence-Storm");

        assertThat(firstAccount.getExtendedAccount().getResourceId()).isEqualTo("4957");
        assertThat(firstAccount.getExtendedAccount().getStatus()).isEqualTo(Status.ENABLED);
        assertThat(firstAccount.getExtendedAccount().getUsage()).isEqualTo(UsageType.PRIVATE);
        assertThat(firstAccount.getExtendedAccount().getBic()).isEqualTo("BUNQNL2A");
        assertThat(firstAccount.getExtendedAccount().getAccountReferences()).containsOnly(
                AccountReferenceDTO.builder()
                        .type(AccountReferenceType.IBAN)
                        .value("NL94BUNQ9900054180")
                        .build());
        assertThat(firstAccount.getExtendedAccount().getBalances()).containsOnly(
                BalanceDTO.builder()
                        .balanceType(BalanceType.INTERIM_BOOKED)
                        .balanceAmount(new BalanceAmountDTO(
                                CurrencyCode.valueOf("EUR"),
                                new BigDecimal("0.00"))
                        ).build()
        );
        assertThat(firstAccount.getExtendedAccount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(firstAccount.getExtendedAccount().getName()).isEqualTo("bunq account");
        assertThat(firstAccount.getExtendedAccount().getCashAccountType()).isEqualTo(ExternalCashAccountType.CURRENT);

    }

    @Test
    public void shouldMapExtendedFieldsWhenRetrievingAccountsAndTransactions() throws TokenInvalidException {
        // given
        BunqApiContext context = new BunqApiContext("1", "serverToken", SecurityUtils.generateKeyPair(), "oauthToken", "sessionToken", 1L);
        ExtendedTransactionDTO expectedDebitTransaction = ExtendedTransactionDTO.builder()
                .bookingDate(ZonedDateTime.from(LocalDateTime.parse("2018-09-04 13:49:03.006898", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")).atZone(AMSTERDAM_TIMEZONE)))
                .status(TransactionStatus.BOOKED)
                .transactionAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("-20.00")))
                .remittanceInformationUnstructured("testPayment")
                .debtorName("B. Schipper")
                .debtorAccount(new AccountReferenceDTO(AccountReferenceType.IBAN, "NL58BUNQ9900053761"))
                .creditorName("duder")
                .creditorAccount(new AccountReferenceDTO(AccountReferenceType.IBAN, "NL23ABNA0581113566"))
                .build();

        ExtendedTransactionDTO expectedCreditTransaction = ExtendedTransactionDTO.builder()
                .bookingDate(ZonedDateTime.from(LocalDateTime.parse("2018-09-04 13:49:02.772081", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")).atZone(AMSTERDAM_TIMEZONE)))
                .status(TransactionStatus.BOOKED)
                .transactionAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("500.00")))
                .remittanceInformationUnstructured("")
                .debtorName("S. Daddy")
                .debtorAccount(new AccountReferenceDTO(AccountReferenceType.IBAN, "NL65BUNQ9900000188"))
                .creditorName("B. Schipper")
                .creditorAccount(new AccountReferenceDTO(AccountReferenceType.IBAN, "NL58BUNQ9900053761"))
                .build();

        // when
        List<ProviderTransactionDTO> transactions = accountsAndTransactionsService.fetchTransactionsForAccount(httpService, context, "1", TRANSACTIONS_FETCH_START_TIME);

        // then
        assertThat(transactions.get(0).getExtendedTransaction()).isEqualTo(expectedDebitTransaction);
        assertThat(transactions.get(1).getExtendedTransaction()).isEqualTo(expectedCreditTransaction);
    }

    @Test
    public void shouldThrowTokenInvalidExceptionWhenBunqRespondsWith401() {
        // given
        BunqApiContext context = new BunqApiContext("1", "serverToken", SecurityUtils.generateKeyPair(), "oauthToken", "give401", 1L);

        // when
        ThrowableAssert.ThrowingCallable accountsAndTransactionsCallable = () -> accountsAndTransactionsService.fetchAccountsAndTransactionsForUser(context, httpService, TRANSACTIONS_FETCH_START_TIME);

        // then
        assertThatThrownBy(accountsAndTransactionsCallable).isExactlyInstanceOf(TokenInvalidException.class);
    }
}

