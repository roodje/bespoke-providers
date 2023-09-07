package com.yolt.providers.fineco;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.FakeRestTemplateManager;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.fineco.dto.FinecoAccessMeans;
import com.yolt.providers.fineco.exception.FinecoMalformedException;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test contains all happy flows occurring in Fineco provider.
 * Covered flows:
 * - acquiring consent page
 * - creating access means
 * - throwing TokenInvalidException for refreshing access means (Fineco does not support token refresh)
 * - fetching accounts, balances, transactions
 * <p>
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = FinecoTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/mappings/ais/happy_flow", httpsPort = 0, port = 0)
@ActiveProfiles("fineco")
public class FinecoDataProviderHappyFlowIntegrationTest {

    private static final String CONSENT_ID = "c3a1880e-cd10-4833-8edb-3296beee5247";
    private static final String CONSENT_URL = "https://sandbox.api.finceobank.com/v1/consent";
    private static final String REDIRECT_URL = "https://www.yolt.com/callback-acc";
    private static final String STATE = "8b6dee15-ea2a-49b2-b100-f5f96d31cd90";

    @Autowired
    private FinecoDataProviderV3 provider;

    @Autowired
    @Qualifier("FinecoObjectMapper")
    private ObjectMapper mapper;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    Stream<UrlDataProvider> finecoProviders() {
        return Stream.of(provider);
    }

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private RestTemplateManager restTemplateManager;

    @BeforeEach
    public void setup() throws IOException, URISyntaxException {
        authenticationMeans = new FinecoSampleTypedAuthenticationMeans().getAuthenticationMeans();
        restTemplateManager = new FakeRestTemplateManager(externalRestTemplateBuilderFactory);
    }

    @ParameterizedTest
    @MethodSource("finecoProviders")
    public void shouldReturnConsentPageUrl(UrlDataProvider providerUnderTest) {
        // given
        Instant createTime = LocalDate.now().atStartOfDay(ZoneId.of("Europe/Rome")).toInstant();
        Instant expireTime = createTime.plus(90, ChronoUnit.DAYS);
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(STATE)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("1.1.1.1")
                .build();

        // when
        Step step = providerUnderTest.getLoginInfo(request);

        // then
        FinecoAccessMeans accessMeans = retrieveAccessMeans(step.getProviderState());
        assertThat(accessMeans.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(accessMeans.getConsentCreateTime()).isBetween(createTime, Instant.now());
        assertThat(accessMeans.getConsentExpireTime()).isEqualTo(expireTime);
        assertThat(step).isInstanceOf(RedirectStep.class);
        RedirectStep redirectStep = (RedirectStep) step;
        assertThat(redirectStep.getRedirectUrl()).isEqualTo(CONSENT_URL);
    }

    @ParameterizedTest
    @MethodSource("finecoProviders")
    public void shouldCreateNewAccessMeans(UrlDataProvider providerUnderTest) {
        // given
        UUID userId = UUID.randomUUID();
        Instant consentCreateTime = Instant.ofEpochMilli(999);
        Instant consentExpireTime = Instant.ofEpochMilli(9999);
        FinecoAccessMeans setUpAccessMeans = new FinecoAccessMeans("consentId", consentCreateTime, consentExpireTime);

        // when
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setProviderState(compactAccessMeans(setUpAccessMeans))
                .setSigner(signer)
                .build();
        AccessMeansOrStepDTO accessMeansOrStep = providerUnderTest.createNewAccessMeans(urlCreateAccessMeans);

        // then
        AccessMeansDTO resultAccessMeans = accessMeansOrStep.getAccessMeans();
        assertThat(resultAccessMeans.getUserId()).isEqualTo(userId);
        assertThat(resultAccessMeans.getExpireTime()).isEqualTo(new Date(consentExpireTime.toEpochMilli()));
        assertThat(resultAccessMeans.getUpdated()).isEqualTo(new Date(consentCreateTime.toEpochMilli()));
        assertThat(retrieveAccessMeans(resultAccessMeans.getAccessMeans())).isEqualTo(setUpAccessMeans);
    }

    @ParameterizedTest
    @MethodSource("finecoProviders")
    public void shouldThrowTokenInvalidExceptionWhenRefreshAccessMeansIsNotImplementedByBankSide(UrlDataProvider providerUnderTest) {
        // when
        ThrowableAssert.ThrowingCallable fetchDataCallable = () -> providerUnderTest.refreshAccessMeans(null);

        // then
        assertThatThrownBy(fetchDataCallable).isExactlyInstanceOf(TokenInvalidException.class);
    }


    @ParameterizedTest
    @MethodSource("finecoProviders")
    public void shouldCorrectlyFetchDataWithFailingCardAccounts(UrlDataProvider providerUnderTest) throws TokenInvalidException, ProviderFetchDataException {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(UUID.randomUUID(),
                "{\"consentId\":\"consentId\"," +
                        "\"consentCreateTime\":\"1970-01-01T00:00:00.999Z\"," +
                        "\"consentExpireTime\":\"1970-01-01T00:00:01.999Z\"}",
                new Date(),
                new Date());

        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(ZonedDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneId.of("Z")).toInstant())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("2.2.2.2")
                .build();

        // when
        DataProviderResponse dataProviderResponse = providerUnderTest.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("finecoProviders")
    public void shouldCorrectlyFetchData(UrlDataProvider providerUnderTest) throws TokenInvalidException, ProviderFetchDataException {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(UUID.randomUUID(),
                "{\"consentId\":\"consentId\"," +
                        "\"consentCreateTime\":\"1970-01-01T00:00:00.999Z\"," +
                        "\"consentExpireTime\":\"1970-01-01T00:00:01.999Z\"}",
                new Date(),
                new Date());

        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(ZonedDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneId.of("Z")).toInstant())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("1.1.1.1")
                .build();

        // when
        DataProviderResponse dataProviderResponse = providerUnderTest.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(2);

        // current account
        ProviderAccountDTO currentAccount = dataProviderResponse.getAccounts().get(0);
        currentAccount.validate();

        verifyCurrentAccount(currentAccount);
        verifyExtendedAccountForCurrentAccount(currentAccount.getExtendedAccount());
        verifyTransactionsForCurrentAccount(currentAccount.getTransactions());
        verifyOneGivenExtendedTransactionForCurrentAccount(currentAccount.getTransactions().get(0).getExtendedTransaction());

        // card account
        ProviderAccountDTO cardAccount = dataProviderResponse.getAccounts().get(1);
        cardAccount.validate();

        verifyCardAccount(cardAccount);
        verifyExtendedAccountForCardAccount(cardAccount.getExtendedAccount());
        verifyTransactionsForCardAccount(cardAccount.getTransactions());
        verifyOneGivenExtendedTransactionForCardAccount(cardAccount.getTransactions().get(0).getExtendedTransaction());
    }

    private void verifyCurrentAccount(ProviderAccountDTO account) {
        ProviderAccountNumberDTO providerAccountNumberDTO = new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, "IT31X0301503200000003517230");
        providerAccountNumberDTO.setHolderName("FirstName LastName");

        assertThat(account.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(account.getCurrentBalance()).isEqualTo("-1000.00");
        assertThat(account.getAvailableBalance()).isEqualTo("6000.00");
        assertThat(account.getAccountId()).isEqualTo("123");
        assertThat(account.getAccountNumber()).isEqualTo(providerAccountNumberDTO);
        assertThat(account.getName()).isEqualTo("Main account EUR");
        assertThat(account.getCreditCardData()).isNull();
    }

    private void verifyCardAccount(ProviderAccountDTO account) {
        assertThat(account.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
        assertThat(account.getCurrentBalance()).isEqualTo("6000.00");
        assertThat(account.getAvailableBalance()).isEqualTo("6000.00");
        assertThat(account.getAccountId()).isEqualTo("456");
        assertThat(account.getAccountNumber()).isNull();
        assertThat(account.getName()).isEqualTo("Card Alias");
        assertThat(account.getCreditCardData()).isNotNull();
        assertThat(account.getCreditCardData().getAvailableCreditAmount()).isEqualTo("6000.00");
        assertThat(account.getAccountMaskedIdentification()).isEqualTo("1234567891234567");
    }

    private void verifyExtendedAccountForCurrentAccount(ExtendedAccountDTO extendedAccount) {
        assertThat(extendedAccount.getResourceId()).isEqualTo("123");
        assertThat(extendedAccount.getAccountReferences()).isEqualTo(Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, "IT31X0301503200000003517230")));
        assertThat(extendedAccount.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extendedAccount.getProduct()).isEqualTo("Main account EUR");
        assertThat(extendedAccount.getCashAccountType()).isEqualTo(ExternalCashAccountType.CURRENT);
        assertThat(extendedAccount.getBalances()).isEqualTo(
                Arrays.asList(
                        expectedBalancesForAccount("EUR", "6000.00", "interimAvailable", null),
                        expectedBalancesForAccount("EUR", "-500.00", "forwardAvailable", null),
                        expectedBalancesForAccount("EUR", "-1000.00", "interimBooked", null)
                )
        );
    }

    private void verifyExtendedAccountForCardAccount(ExtendedAccountDTO extendedAccount) {
        assertThat(extendedAccount.getResourceId()).isEqualTo("456");
        assertThat(extendedAccount.getAccountReferences()).isNull();
        assertThat(extendedAccount.getCurrency()).isEqualTo(CurrencyCode.USD);
        assertThat(extendedAccount.getName()).isEqualTo("Card Alias");
        assertThat(extendedAccount.getProduct()).isEqualTo("Multicurrency USD");
        assertThat(extendedAccount.getCashAccountType()).isEqualTo(ExternalCashAccountType.CURRENT);
        assertThat(extendedAccount.getBalances()).isEqualTo(
                Arrays.asList(
                        expectedBalancesForAccount("EUR", "6000.00", "interimAvailable", "2019-02-28"),
                        expectedBalancesForAccount("EUR", "-500.00", "interimAvailable", "2019-02-28"),
                        expectedBalancesForAccount("EUR", "300.00", "interimAvailable", "2019-02-28")
                )
        );
    }

    private void verifyTransactionsForCurrentAccount(List<ProviderTransactionDTO> transactions) {
        assertThat(transactions).hasSize(2);

        assertThat(transactions.get(0).getDateTime()).isEqualTo("2019-01-25T00:00+01:00[Europe/Rome]");
        assertThat(transactions.get(0).getAmount()).isEqualTo("256.67");
        assertThat(transactions.get(0).getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transactions.get(0).getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transactions.get(0).getDescription()).isEqualTo("causale pagamento");
        assertThat(transactions.get(0).getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transactions.get(0).getMerchant()).isEqualTo("Walter Bianchi");

        assertThat(transactions.get(1).getDateTime()).isEqualTo("2019-01-25T00:00+01:00[Europe/Rome]");
        assertThat(transactions.get(1).getAmount()).isEqualTo("343.01");
        assertThat(transactions.get(1).getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transactions.get(1).getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transactions.get(1).getDescription()).isEqualTo("causale pagamento2");
        assertThat(transactions.get(1).getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transactions.get(1).getMerchant()).isNull();
    }

    private void verifyTransactionsForCardAccount(List<ProviderTransactionDTO> transactions) {
        assertThat(transactions).hasSize(2);

        assertThat(transactions.get(0).getDateTime()).isEqualTo("2018-11-05T00:00+01:00[Europe/Rome]");
        assertThat(transactions.get(0).getAmount()).isEqualTo("255.67");
        assertThat(transactions.get(0).getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transactions.get(0).getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transactions.get(0).getDescription()).isEqualTo("causale pagamento");
        assertThat(transactions.get(0).getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transactions.get(0).getMerchant()).isNull();

        assertThat(transactions.get(1).getDateTime()).isEqualTo("2019-11-05T00:00+01:00[Europe/Rome]");
        assertThat(transactions.get(1).getAmount()).isEqualTo("256.67");
        assertThat(transactions.get(1).getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transactions.get(1).getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transactions.get(1).getDescription()).isEqualTo("causale pagamento2");
        assertThat(transactions.get(1).getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transactions.get(1).getMerchant()).isNull();
    }

    private void verifyOneGivenExtendedTransactionForCurrentAccount(ExtendedTransactionDTO extendedTransaction) {
        assertThat(extendedTransaction.getMandateId()).isNull();
        assertThat(extendedTransaction.getCheckId()).isNull();
        assertThat(extendedTransaction.getCreditorId()).isNull();
        assertThat(extendedTransaction.getBookingDate()).isEqualTo(LocalDate.parse("2019-01-25").atStartOfDay(ZoneId.of("Europe/Rome")));
        assertThat(extendedTransaction.getValueDate()).isEqualTo(LocalDate.parse("2019-01-26").atStartOfDay(ZoneId.of("Europe/Rome")));
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo("-256.67");
        assertThat(extendedTransaction.getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extendedTransaction.getCreditorName()).isEqualTo("Walter Bianchi");
        assertThat(extendedTransaction.getUltimateCreditor()).isNull();
        assertThat(extendedTransaction.getDebtorName()).isNull();
        assertThat(extendedTransaction.getUltimateDebtor()).isNull();
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("causale pagamento");
        assertThat(extendedTransaction.getBankTransactionCode()).isNull();
        assertThat(extendedTransaction.getProprietaryBankTransactionCode()).isEqualTo("W43-Pagamento Visa Debit");
        assertThat(extendedTransaction.isTransactionIdGenerated()).isTrue();
    }

    private void verifyOneGivenExtendedTransactionForCardAccount(ExtendedTransactionDTO extendedTransaction) {
        assertThat(extendedTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(extendedTransaction.getBookingDate()).isEqualTo(LocalDate.parse("2018-11-05").atStartOfDay(ZoneId.of("Europe/Rome")));
        assertThat(extendedTransaction.getValueDate()).isEqualTo(LocalDate.parse("2019-02-05").atStartOfDay(ZoneId.of("Europe/Rome")));
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo("-255.67");
        assertThat(extendedTransaction.getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(extendedTransaction.getCreditorAccount()).isNotNull();
        assertThat(extendedTransaction.getCreditorAccount()).isEqualTo(new AccountReferenceDTO(AccountReferenceType.MASKED_PAN, "1234567891234567"));
        assertThat(extendedTransaction.isTransactionIdGenerated()).isTrue();
    }

    private BalanceDTO expectedBalancesForAccount(String currency,
                                                  String amount,
                                                  String balanceType,
                                                  String referenceDate) {
        return BalanceDTO.builder()
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.valueOf(currency), new BigDecimal(amount)))
                .balanceType(BalanceType.fromName(balanceType))
                .referenceDate(Objects.nonNull(referenceDate) ? (LocalDate.parse(referenceDate).atStartOfDay(ZoneId.of("Europe/Rome"))) : null)
                .build();
    }

    private String compactAccessMeans(final FinecoAccessMeans accessMeansDTO) {
        try {
            return mapper.writeValueAsString(accessMeansDTO);
        } catch (JsonProcessingException e) {
            throw new FinecoMalformedException("Error creating json access means", e);
        }
    }

    private FinecoAccessMeans retrieveAccessMeans(final String providerState) {
        try {
            return mapper.readValue(providerState, FinecoAccessMeans.class);
        } catch (IOException e) {
            throw new FinecoMalformedException("Error reading Fineco Access Means", e);
        }
    }
}
