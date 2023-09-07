package com.yolt.providers.unicredit.it.ais;

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
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.unicredit.AccessMeansTestMapper;
import com.yolt.providers.unicredit.TestApp;
import com.yolt.providers.unicredit.UnicreditSampleTypedAuthenticationMeans;
import com.yolt.providers.unicredit.common.ais.UniCreditDataProvider;
import com.yolt.providers.unicredit.common.dto.UniCreditAccessMeansDTO;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExchangeRateDTO;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.unicredit.common.auth.UniCreditAuthMeans.REGISTRATION_STATUS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/unicredit/it/ais/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("unicredit")
public class UniCreditItDataProviderHappyFlowIntegrationTest {

    private static final String CONSENT_ID = "c3a1880e-cd10-4833-8edb-3296beee5247";
    private static final String CONSENT_URL = "https://authorization.api-sandbox.unicredit.eu:8403/sandbox/psd2/bg/loginPSD2_BG.html?consentId=c3a1880e-cd10-4833-8edb-3296beee5247&correlationid=cSvzeI&country=IT";
    private static final String REDIRECT_URL = "https://www.yolt.com/callback-acc";
    private static final String STATE = "8b6dee15-ea2a-49b2-b100-f5f96d31cd90";
    private static final String CERT_PATH = "certificates/unicredit/unicredit_certificate.pem";
    private static final String PST_IP_ADDRESS = "192.160.1.2";

    @Mock
    private Signer signer;

    @Autowired
    @Qualifier("Unicredit")
    private ObjectMapper objectMapper;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    private RestTemplateManager restTemplateManager;

    private UnicreditSampleTypedAuthenticationMeans testAuthenticationMeans;

    @Qualifier("UniCreditDataProviderV4")
    @Autowired
    private UniCreditDataProvider dataProvider;

    @BeforeEach
    public void setup() throws Exception {
        testAuthenticationMeans = new UnicreditSampleTypedAuthenticationMeans(CERT_PATH);
        restTemplateManager = new FakeRestTemplateManager(externalRestTemplateBuilderFactory);
    }

    @Test
    public void shouldReturnClientConfigurationMeansForAutoConfigurationMeansWithCorrectRequestData() {
        // given
        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequest(
                testAuthenticationMeans.getAuthMeans(), restTemplateManager, signer, null);

        // when
        Map<String, BasicAuthenticationMean> configureMeans = dataProvider.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(configureMeans).containsKey(REGISTRATION_STATUS);
    }

    @Test
    public void shouldReturnRedirectStepWithConsentUrlAndCorrectAccessMeansForGetLoginInfoWithCorrectRequestData() {
        // given
        Instant now = Instant.now();
        Instant consentExpiration = LocalDate.now().atStartOfDay(ZoneId.of("Europe/Rome")).toInstant().plus(90, ChronoUnit.DAYS);
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL).setState(STATE)
                .setAuthenticationMeans(testAuthenticationMeans.getAuthMeans())
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PST_IP_ADDRESS)
                .build();

        // when
        Step step = dataProvider.getLoginInfo(request);

        // then
        UniCreditAccessMeansDTO accessMeans = AccessMeansTestMapper.with(objectMapper).retrieveAccessMeans(step.getProviderState());
        assertThat(accessMeans.getConsentId()).isEqualTo(CONSENT_ID);
        assertThat(accessMeans.getCreated()).isBetween(now, Instant.now());
        assertThat(accessMeans.getExpireTime()).isEqualTo(consentExpiration);
        assertThat(step).isInstanceOf(RedirectStep.class);
        RedirectStep redirectStep = (RedirectStep) step;
        assertThat(redirectStep.getRedirectUrl()).isEqualTo(CONSENT_URL);
    }

    @Test
    public void shouldReturnNewAccessMeansForCreateNewAccessMeansWithCorrectRequestData() {
        // given
        AccessMeansTestMapper accessMeansTestMapper = AccessMeansTestMapper.with(objectMapper);
        UUID userId = UUID.randomUUID();
        UniCreditAccessMeansDTO setUpAccessMeans = new UniCreditAccessMeansDTO("consentId", Instant.ofEpochMilli(999), Instant.ofEpochMilli(9999));
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setProviderState(accessMeansTestMapper.compactAccessMeans(setUpAccessMeans))
                .setSigner(signer)
                .setPsuIpAddress(PST_IP_ADDRESS)
                .build();

        // when
        AccessMeansOrStepDTO accessMeansOrStep = dataProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        AccessMeansDTO resultAccessMeans = accessMeansOrStep.getAccessMeans();
        assertThat(resultAccessMeans.getUserId()).isEqualTo(userId);
        assertThat(resultAccessMeans.getExpireTime()).isEqualTo(new Date(Instant.ofEpochMilli(9999).toEpochMilli()));
        assertThat(resultAccessMeans.getUpdated()).isEqualTo(new Date(Instant.ofEpochMilli(999).toEpochMilli()));
        assertThat(accessMeansTestMapper.retrieveAccessMeans(resultAccessMeans.getAccessMeans())).isEqualTo(setUpAccessMeans);
    }

    @Test
    public void shouldThrowTokenInvalidExceptionForRefreshAccessMeansAsThisFeatureIsUnsupported() {
        // when
        ThrowableAssert.ThrowingCallable refreshAccessMeansCallable = () -> dataProvider.refreshAccessMeans(null);

        // then
        assertThatThrownBy(refreshAccessMeansCallable)
                .isInstanceOf(TokenInvalidException.class);
    }

    @Test
    public void shouldReturnDataProviderResponseForFetchDataWithCorrectRequestData() throws ProviderFetchDataException, TokenInvalidException {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(UUID.randomUUID(),
                "{\"consentId\":\"consentId\"}",
                new Date(),
                new Date());
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(testAuthenticationMeans.getAuthMeans())
                .setSigner(signer)
                .setTransactionsFetchStartTime(ZonedDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneId.of("Z")).toInstant())
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PST_IP_ADDRESS)
                .build();

        // when
        DataProviderResponse response = dataProvider.fetchData(request);

        // then
        assertAccounts(response.getAccounts());
        assertTransactionsHaveExpectedValues(response);
    }

    private void assertAccounts(final List<ProviderAccountDTO> accounts) {
        accounts.forEach(ProviderAccountDTO::validate);
        assertThat(accounts).hasSize(2);

        ProviderAccountDTO firstAccount = accounts.get(0);
        ProviderAccountDTO secondAccount = accounts.get(1);

        firstAccount.validate();
        secondAccount.validate();

        assertThat(firstAccount.getAccountId()).isEqualTo("123");
        assertThat(firstAccount.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(firstAccount.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(firstAccount.getBic()).isEqualTo("BPKOPLPW");
        assertThat(firstAccount.getName()).isEqualTo("Account-123");
        assertThat(firstAccount.getCurrentBalance()).isEqualTo("123456.78");
        assertThat(firstAccount.getAvailableBalance()).isEqualTo("234567.89");
        assertThat(firstAccount.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(firstAccount.getAccountNumber().getIdentification()).isEqualTo("DE2310010010123456789");

        ExtendedAccountDTO firstExtendedAccount = firstAccount.getExtendedAccount();
        assertThat(firstExtendedAccount.getResourceId()).isEqualTo("123");
        assertThat(firstExtendedAccount.getBic()).isEqualTo("BPKOPLPW");
        assertThat(firstExtendedAccount.getCashAccountType()).isEqualTo(ExternalCashAccountType.CURRENT);
        assertThat(firstExtendedAccount.getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(firstExtendedAccount.getAccountReferences()).hasSize(1);
        assertThat(firstExtendedAccount.getAccountReferences().get(0).getValue()).isEqualTo("DE2310010010123456789");
        assertThat(firstExtendedAccount.getAccountReferences().get(0).getType()).isEqualTo(AccountReferenceType.IBAN);

        BalanceDTO firstBalanceForFirstAccount = firstExtendedAccount.getBalances().get(0);
        assertThat(firstBalanceForFirstAccount.getBalanceAmount().getAmount()).isEqualTo("123456.78");
        assertThat(firstBalanceForFirstAccount.getBalanceAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(firstBalanceForFirstAccount.getBalanceType()).isEqualTo(BalanceType.EXPECTED);
        assertThat(firstBalanceForFirstAccount.getReferenceDate()).isEqualTo(
                ZonedDateTime.of(2019, 10, 14, 0, 0, 0, 0, ZoneId.of("Z")));

        BalanceDTO secondBalanceForFirstAccount = firstExtendedAccount.getBalances().get(1);
        assertThat(secondBalanceForFirstAccount.getBalanceAmount().getAmount()).isEqualTo("234567.89");
        assertThat(secondBalanceForFirstAccount.getBalanceAmount().getCurrency()).isEqualTo(CurrencyCode.EUR);
        assertThat(secondBalanceForFirstAccount.getBalanceType()).isEqualTo(BalanceType.INTERIM_AVAILABLE);
        assertThat(secondBalanceForFirstAccount.getReferenceDate()).isEqualTo(
                ZonedDateTime.of(2019, 10, 13, 0, 0, 0, 0, ZoneId.of("Z")));

        assertThat(secondAccount.getAccountId()).isEqualTo("456");
        assertThat(secondAccount.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
        assertThat(secondAccount.getAccountNumber().getIdentification()).isEqualTo("DE2310010010123499999");
        BalanceDTO firstBalanceForSecondAccount = secondAccount.getExtendedAccount().getBalances().get(0);
        assertThat(firstBalanceForSecondAccount.getBalanceAmount().getAmount()).isEqualTo("4654654.78");
        assertThat(secondAccount.getCreditCardData().getAvailableCreditAmount()).isEqualTo("2342344.89");
        BalanceDTO secondBalanceForSecondAccount = secondAccount.getExtendedAccount().getBalances().get(1);
        assertThat(secondBalanceForSecondAccount.getBalanceAmount().getAmount()).isEqualTo("2342344.89");
        assertThat(secondAccount.getExtendedAccount().getCashAccountType()).isEqualTo(ExternalCashAccountType.CASH_INCOME);
    }

    private void assertTransactionsHaveExpectedValues(final DataProviderResponse response) {
        List<ProviderAccountDTO> accounts = response.getAccounts();
        ProviderAccountDTO account0 = accounts.get(0);
        List<ProviderTransactionDTO> transactions0 = account0.getTransactions();
        assertThat(transactions0).hasSize(4);

        ProviderTransactionDTO transaction00 = transactions0.get(0);
        assertThat(transaction00.getDateTime()).isEqualTo("2008-09-15T00:00+02:00[Europe/Rome]");
        assertThat(transaction00.getAmount()).isEqualTo("43.0");
        assertThat(transaction00.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction00.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction00.getMerchant()).isEqualTo("creditor");

        ExtendedTransactionDTO extTransaction00 = transaction00.getExtendedTransaction();
        assertThat(extTransaction00.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(extTransaction00.getEntryReference()).isEqualTo("23");
        assertThat(extTransaction00.getEndToEndId()).isEqualTo("34");
        assertThat(extTransaction00.getMandateId()).isEqualTo("45");
        assertThat(extTransaction00.getCheckId()).isEqualTo("56");
        assertThat(extTransaction00.getCreditorId()).isEqualTo("67");
        assertThat(extTransaction00.getBookingDate()).isEqualTo("2008-09-15T00:00+02:00[Europe/Rome]");
        assertThat(extTransaction00.getValueDate()).isEqualTo("2008-09-16T00:00+02:00[Europe/Rome]");
        assertThat(extTransaction00.getTransactionAmount())
                .extracting(BalanceAmountDTO::getCurrency, BalanceAmountDTO::getAmount)
                .contains(CurrencyCode.EUR, BigDecimal.valueOf(-43.00));
        assertThat(extTransaction00.getCreditorName()).isEqualTo("creditor");
        assertThat(extTransaction00.getCreditorAccount())
                .extracting(AccountReferenceDTO::getType, AccountReferenceDTO::getValue)
                .contains(AccountReferenceType.IBAN, "43");
        assertThat(extTransaction00.getUltimateCreditor()).isEqualTo("ultimateCreditor");
        assertThat(extTransaction00.getDebtorName()).isNullOrEmpty();
        assertThat(extTransaction00.getDebtorAccount()).isNull();
        assertThat(extTransaction00.getUltimateDebtor()).isNullOrEmpty();
        assertThat(extTransaction00.getRemittanceInformationUnstructured()).isEqualTo("Unstructured");
        assertThat(extTransaction00.getRemittanceInformationStructured()).isEqualTo("Structured");
        assertThat(extTransaction00.getPurposeCode()).isEqualTo("BKFM");
        assertThat(extTransaction00.getBankTransactionCode()).isEqualTo("ACT");
        assertThat(extTransaction00.getProprietaryBankTransactionCode()).isEqualTo("143");
        assertThat(extTransaction00.isTransactionIdGenerated()).isFalse();

        List<ExchangeRateDTO> exchangeRates00 = extTransaction00.getExchangeRate();
        assertThat(exchangeRates00).hasSize(2);

        ExchangeRateDTO exchangeRate000 = exchangeRates00.get(0);
        assertThat(exchangeRate000.getCurrencyFrom()).isEqualTo(CurrencyCode.EUR);
        assertThat(exchangeRate000.getRateFrom()).isEqualTo("rateFrom1");
        assertThat(exchangeRate000.getCurrencyTo()).isEqualTo(CurrencyCode.PLN);
        assertThat(exchangeRate000.getRateTo()).isEqualTo("rateTo1");
        assertThat(exchangeRate000.getRateDate()).isEqualTo("2008-09-17T00:00+02:00[Europe/Rome]");
        assertThat(exchangeRate000.getRateContract()).isEqualTo("rateContract1");

        ExchangeRateDTO exchangeRate001 = extTransaction00.getExchangeRate().get(1);
        assertThat(exchangeRate001.getRateFrom()).isEqualTo("rateFrom2");
    }
}
