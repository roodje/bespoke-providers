package com.yolt.providers.deutschebank.de;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.deutschebank.TestConfiguration;
import com.yolt.providers.deutschebank.common.domain.DeutscheBankGroupProviderState;
import com.yolt.providers.deutschebank.common.mapper.DeutscheBankGroupProviderStateMapper;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.TextField;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CERTIFICATE_PEM;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.KEY_ID;
import static com.yolt.providers.deutschebank.common.auth.DeutscheBankGroupAuthenticationMeansProducerV1.TRANSPORT_CERTIFICATE_NAME;
import static com.yolt.providers.deutschebank.common.auth.DeutscheBankGroupAuthenticationMeansProducerV1.TRANSPORT_KEY_ID_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.PENDING;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = TestConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/happy-flow/", httpsPort = 0, port = 0)
@ActiveProfiles("deutschebank")
class DeutscheBankDataProviderHappyFlowIntegrationTest {

    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final Date UPDATED_DATE = parseDate("2020-01-01");
    private static final Date EXPIRATION_DATE = parseDate("2020-01-02");
    private static final String CONSENT_ID = "7a7251ff-45ef-4e24-a4cc-bb77d4ba0b16";
    private static final String REDIRECT_URI = "https://yolt.com/callback";

    @Autowired
    @Qualifier("DeutscheBankGroupObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private DeutscheBankDataProviderV1 dataProvider;

    @Mock
    private Signer signer;

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private DeutscheBankGroupProviderStateMapper providerStateMapper;

    @BeforeEach
    void initialize() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:certificates/fake-certificate.pem");
        String pemCertificate = String.join("\n", Files.readAllLines(resource.getFile().toPath(), UTF_8));

        providerStateMapper = new DeutscheBankGroupProviderStateMapper(objectMapper);
        authenticationMeans = new HashMap<>();
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), pemCertificate));
        authenticationMeans.put(TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), "2be4d475-f240-42c7-a22c-882566ac0f95"));
    }

    @Test
    void shouldReturnTypedAuthenticationMeans() {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthMeans).hasSize(2);
        assertThat(typedAuthMeans.get(TRANSPORT_KEY_ID_NAME)).isEqualTo(KEY_ID);
        assertThat(typedAuthMeans.get(TRANSPORT_CERTIFICATE_NAME)).isEqualTo(CERTIFICATE_PEM);
    }

    @Test
    void shouldReturnFormStepWithUserIDField() {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setState("d79960be-3058-4d8e-9cd3-3753febd4661")
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .build();

        // when
        FormStep formStep = dataProvider.getLoginInfo(request);

        // then
        assertThat(formStep.getEncryptionDetails()).isEqualTo(EncryptionDetails.noEncryption());
        assertThat(formStep.getTimeoutTime()).isCloseTo(Instant.now().plus(1L, HOURS), within(10, SECONDS));

        Form form = formStep.getForm();
        assertThat(form.getFormComponents()).hasSize(2);

        TextField branchNumber = (TextField) form.getFormComponents().get(0);
        assertThat(branchNumber.getId()).isEqualTo("branch-number");
        assertThat(branchNumber.getDisplayName()).isEqualTo("Branch Number (three-digit)");
        assertThat(branchNumber.getLength()).isEqualTo(0);
        assertThat(branchNumber.getMaxLength()).isEqualTo(3);
        assertThat(branchNumber.getOptional()).isFalse();

        TextField accountNumber = (TextField) form.getFormComponents().get(1);
        assertThat(accountNumber.getId()).isEqualTo("account-number");
        assertThat(accountNumber.getDisplayName()).isEqualTo("Account Number (seven-digit, without sub-account number)");
        assertThat(accountNumber.getLength()).isEqualTo(0);
        assertThat(accountNumber.getMaxLength()).isEqualTo(7);
        assertThat(accountNumber.getOptional()).isFalse();
    }

    @Test
    void shouldReturnRedirectStepToInitiatedConsentPage() {
        // given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .setSigner(signer)
                .setState("7c3e98de-0239-4868-ada8-aefb5384ef0a")
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setProviderState(providerStateMapper.toJson(createProviderState()))
                .setFilledInUserSiteFormValues(createFilledInUserSiteFormValues())
                .setUserId(USER_ID)
                .build();

        // when
        AccessMeansOrStepDTO accessMeansOrStepDTO = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(accessMeansOrStepDTO.getAccessMeans()).isNull();
        RedirectStep redirectStep = (RedirectStep) accessMeansOrStepDTO.getStep();

        String loginUrl = redirectStep.getRedirectUrl();
        assertThat(loginUrl).contains("/authorize");

        Map<String, String> queryParams = getQueryParamsFromUrl(loginUrl);
        assertThat(queryParams).isEmpty();
    }

    @Test
    void shouldReturnNewAccessMeans() throws TokenInvalidException {
        // given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .setSigner(signer)
                .setState("29dbba15-1e67-4ac0-ab0f-2487dc0c960b")
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setProviderState(providerStateMapper.toJson(createProviderState()))
                .setRedirectUrlPostedBackFromSite("https://yolt.com/callback?state=29dbba15-1e67-4ac0-ab0f-2487dc0c960b&code=authorization-code")
                .setUserId(USER_ID)
                .build();

        // when
        AccessMeansOrStepDTO accessMeansOrStepDTO = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(accessMeansOrStepDTO.getStep()).isNull();

        AccessMeansDTO accessMeansDTO = accessMeansOrStepDTO.getAccessMeans();
        assertThat(accessMeansDTO.getUserId()).isEqualTo(request.getUserId());
        assertThat(accessMeansDTO.getUpdated()).isAfter(UPDATED_DATE);
        assertThat(accessMeansDTO.getExpireTime()).isAfter(EXPIRATION_DATE);

        DeutscheBankGroupProviderState providerState = providerStateMapper.fromJson(accessMeansDTO.getAccessMeans());
        assertThat(providerState.getConsentId()).isEqualTo(CONSENT_ID);
    }

    @Test
    void shouldReturnTokenInvalidExceptionDuringRefreshAccessMeans() {
        // given
        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setAccessMeans(createAccessMeansDTO())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        ThrowableAssert.ThrowingCallable refreshAccessMeansCallable = () -> dataProvider.refreshAccessMeans(request);

        // then
        assertThatThrownBy(refreshAccessMeansCallable).isExactlyInstanceOf(TokenInvalidException.class);
    }

    @Test
    void shouldReturnAccountsAndTransactions() throws TokenInvalidException, ProviderFetchDataException {
        // given
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setAccessMeans(createAccessMeansDTO())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setTransactionsFetchStartTime(Instant.now())
                .build();

        // when
        DataProviderResponse response = dataProvider.fetchData(request);

        // then
        List<ProviderAccountDTO> accounts = response.getAccounts();
        assertThat(accounts).hasSize(2);

        ProviderAccountDTO account1 = accounts.get(0);
        assertThat(account1).satisfies(validateProviderAccountDTO("1", "510.10", "510.10"));

        List<ProviderTransactionDTO> account1Transactions = account1.getTransactions();
        assertThat(account1Transactions).hasSize(4);
        assertThat(account1Transactions.get(0)).satisfies(validateProviderTransactionDTO("313ATCW1823600GV", "0.10", BOOKED, CREDIT, null));
        assertThat(account1Transactions.get(1)).satisfies(validateProviderTransactionDTO("766ATCW18223009Z", "200.10", BOOKED, CREDIT, null));

        ProviderAccountDTO account2 = accounts.get(1);
        assertThat(account2).satisfies(validateProviderAccountDTO("2", "-20.10", "210.10"));

        List<ProviderTransactionDTO> account2Transactions = account2.getTransactions();
        assertThat(account2Transactions).hasSize(4);
        assertThat(account2Transactions.get(0)).satisfies(validateProviderTransactionDTO("011EACH182320054", "0.13", PENDING, DEBIT, "Ref 12345678, Suma platita 252.91 EUR"));
        assertThat(account2Transactions.get(2)).satisfies(validateProviderTransactionDTO("328CHDP182190065", "400.00", BOOKED, DEBIT, "Data_Ora: 23-02-2019 17:10:30"));
        assertThat(account2Transactions.get(3)).satisfies(validateProviderTransactionDTO("026ATCW1822203ZV", "150.00", BOOKED, CREDIT, "Bestätigung der Transaktion - Tranz: Nr card 9999XXXXXX9999"));
    }

    private Consumer<ProviderAccountDTO> validateProviderAccountDTO(String accountId, String availableBalance, String currentBalance) {
        return providerAccountDTO -> {
            providerAccountDTO.validate();

            assertThat(providerAccountDTO.getAccountId()).isEqualTo(accountId);
            assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
            assertThat(providerAccountDTO.getLastRefreshed()).isCloseTo(ZonedDateTime.now(), within(32, SECONDS));
            assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal(availableBalance));
            assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal(currentBalance));
            assertThat(providerAccountDTO.getName()).isNotEmpty();
            assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);

            ProviderAccountNumberDTO accountNumberDTO = providerAccountDTO.getAccountNumber();
            assertThat(accountNumberDTO.getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
            assertThat(accountNumberDTO.getIdentification()).isNotEmpty();

            ExtendedAccountDTO extendedAccountDTO = providerAccountDTO.getExtendedAccount();
            assertThat(extendedAccountDTO.getResourceId()).isEqualTo(accountId);
            assertThat(extendedAccountDTO.getName()).isEqualTo(providerAccountDTO.getName());
            assertThat(extendedAccountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);

            AccountReferenceDTO accountReferenceDTO = extendedAccountDTO.getAccountReferences().get(0);
            assertThat(accountReferenceDTO.getType()).isEqualTo(AccountReferenceType.IBAN);
            assertThat(accountReferenceDTO.getValue()).isEqualTo(accountNumberDTO.getIdentification());

            List<BalanceDTO> balances = extendedAccountDTO.getBalances();
            assertThat(balances).isNotEmpty();
        };
    }

    private Consumer<ProviderTransactionDTO> validateProviderTransactionDTO(String transactionId, String amount, TransactionStatus status, ProviderTransactionType type,
                                                                            String remittanceInformationUnstructured) {
        return providerTransactionDTO -> {
            assertThat(providerTransactionDTO.getExternalId()).isEqualTo(transactionId);
            assertThat(providerTransactionDTO.getDateTime()).isEqualTo("2020-10-03T00:00+02:00[Europe/Berlin]");
            assertThat(providerTransactionDTO.getAmount()).isEqualTo(new BigDecimal(amount));
            assertThat(providerTransactionDTO.getStatus()).isEqualTo(status);
            assertThat(providerTransactionDTO.getType()).isEqualTo(type);
            assertThat(providerTransactionDTO.getDescription()).isNotEmpty();
            assertThat(providerTransactionDTO.getCategory()).isEqualTo(YoltCategory.GENERAL);

            ExtendedTransactionDTO extendedTransactionDTO = providerTransactionDTO.getExtendedTransaction();
            assertThat(extendedTransactionDTO.getStatus()).isEqualTo(status);
            assertThat(extendedTransactionDTO.getBookingDate()).isEqualTo("2020-10-03T00:00+02:00[Europe/Berlin]");
            assertThat(extendedTransactionDTO.getValueDate()).isEqualTo("2020-10-03T00:00+02:00[Europe/Berlin]");
            assertThat(extendedTransactionDTO.getRemittanceInformationUnstructured()).isEqualTo(remittanceInformationUnstructured);

            BalanceAmountDTO balanceAmountDTO = extendedTransactionDTO.getTransactionAmount();
            if (DEBIT.equals(type)) {
                assertThat(balanceAmountDTO.getAmount()).isEqualTo(new BigDecimal(amount).negate());
            }
            if (CREDIT.equals(type)) {
                assertThat(balanceAmountDTO.getAmount()).isEqualTo(new BigDecimal(amount));
            }
            assertThat(balanceAmountDTO.getCurrency()).isEqualTo(CurrencyCode.EUR);
        };
    }

    private AccessMeansDTO createAccessMeansDTO() {
        DeutscheBankGroupProviderState providerState = createProviderState();
        return new AccessMeansDTO(USER_ID, providerStateMapper.toJson(providerState), UPDATED_DATE, EXPIRATION_DATE);
    }

    private DeutscheBankGroupProviderState createProviderState() {
        return new DeutscheBankGroupProviderState(CONSENT_ID);
    }

    private FilledInUserSiteFormValues createFilledInUserSiteFormValues() {
        HashMap<String, String> valueMap = new HashMap<>(1);
        valueMap.put("branch-number", "123");
        valueMap.put("account-number", "1234567");

        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.setValueMap(valueMap);
        return filledInUserSiteFormValues;
    }

    private static Map<String, String> getQueryParamsFromUrl(String url) {
        return UriComponentsBuilder.fromUriString(url).build()
                .getQueryParams()
                .toSingleValueMap();
    }

    private static Date parseDate(String date) {
        return Date.from(LocalDate.parse(date).atStartOfDay().toInstant(UTC));
    }
}