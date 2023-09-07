package com.yolt.providers.deutschebank.it;

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
import com.yolt.providers.deutschebank.common.DeutscheBankGroupDataProviderV2;
import com.yolt.providers.deutschebank.common.domain.DeutscheBankGroupProviderState;
import com.yolt.providers.deutschebank.common.mapper.DeutscheBankGroupProviderStateMapper;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.*;
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
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = TestConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/happy-flow/", httpsPort = 0, port = 0)
@ActiveProfiles("deutschebank")
class DeutscheBankItDataProviderHappyFlowIntegrationTest {

    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final Date UPDATED_DATE = parseDate("2020-01-01");
    private static final Date EXPIRATION_DATE = parseDate("2020-01-02");
    private static final String CONSENT_ID = "7a7251ff-45ef-4e24-a4cc-bb77d4ba0b16";
    private static final String REDIRECT_URI = "https://yolt.com/callback";
    private static final String CONSENT_URL = "https://db.com/authorize";

    @Autowired
    @Qualifier("DeutscheBankGroupObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("DeutscheBankItDataProviderV1")
    private DeutscheBankGroupDataProviderV2 dataProvider;

    @Autowired
    private Clock clock;

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
        assertThat(typedAuthMeans)
                .hasSize(2)
                .containsEntry(TRANSPORT_KEY_ID_NAME, KEY_ID)
                .containsEntry(TRANSPORT_CERTIFICATE_NAME, CERTIFICATE_PEM);
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
        assertThat(form.getFormComponents()).hasSize(1);

        TextField branchNumber = (TextField) form.getFormComponents().get(0);
        assertThat(branchNumber.getId()).isEqualTo("email");
        assertThat(branchNumber.getDisplayName()).isEqualTo("E-mail (structure: user.name@domain.com)");
        assertThat(branchNumber.getOptional()).isFalse();

    }

    @Test
    void shouldReturnConsentPageUrl() {
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
        assertThat(accessMeansOrStepDTO.getStep()).isNotNull();
        assertThat(accessMeansOrStepDTO.getStep()).isInstanceOf(RedirectStep.class);
        RedirectStep redirectStep = (RedirectStep) accessMeansOrStepDTO.getStep();
        assertThat(redirectStep.getRedirectUrl()).isEqualTo(CONSENT_URL);
    }

    @Test
    void shouldReturnNewAccessMeans() throws TokenInvalidException {
        // given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URI)
                .setSigner(signer)
                .setState("7c3e98de-0239-4868-ada8-aefb5384ef0a")
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setProviderState(providerStateMapper.toJson(createProviderState()))
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
        DataProviderResponse expectedResponse = new DataProviderResponse(List.of(
                account1(),
                account2()
        ));

        // when
        DataProviderResponse response = dataProvider.fetchData(request);

        // then
        assertThat(response).usingRecursiveComparison().
                isEqualTo(expectedResponse);
    }

    private ProviderAccountDTO account1() {
        return ProviderAccountDTO.builder()
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .lastRefreshed(clock.instant().atZone(clock.getZone()))
                .availableBalance(new BigDecimal("510.10"))
                .currentBalance(new BigDecimal("510.10"))
                .accountId("1")
                .accountNumber(new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, "DE66500105172915693295"))
                .name("Deutsche Bank (IT) Current Account")
                .currency(CurrencyCode.EUR)
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("1")
                        .accountReferences(List.of(new AccountReferenceDTO(AccountReferenceType.IBAN, "DE66500105172915693295")))
                        .currency(CurrencyCode.EUR)
                        .name("Deutsche Bank (IT) Current Account")
                        .balances(List.of(
                                BalanceDTO.builder()
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(new BigDecimal("510.10"))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .balanceType(BalanceType.INTERIM_AVAILABLE)
                                        .lastChangeDateTime(ZonedDateTime.of(2019, 02, 28, 00, 00, 00, 00, ZoneId.of("Europe/Rome")))
                                        .build()
                        ))
                        .build())
                .transactions(account1Transactions())
                .build();
    }

    private ProviderAccountDTO account2() {
        return ProviderAccountDTO.builder()
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .lastRefreshed(clock.instant().atZone(clock.getZone()))
                .availableBalance(new BigDecimal("-20.10"))
                .currentBalance(new BigDecimal("210.10"))
                .accountId("2")
                .accountNumber(new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, "DE97500105176141527356"))
                .name("Deutsche Bank (IT) Current Account")
                .currency(CurrencyCode.EUR)
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("2")
                        .accountReferences(List.of(new AccountReferenceDTO(AccountReferenceType.IBAN, "DE97500105176141527356")))
                        .currency(CurrencyCode.EUR)
                        .name("Deutsche Bank (IT) Current Account")
                        .balances(List.of(
                                BalanceDTO.builder()
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(new BigDecimal("210.10"))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .balanceType(BalanceType.CLOSING_BOOKED)
                                        .build(),
                                BalanceDTO.builder()
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(new BigDecimal("-20.10"))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .balanceType(BalanceType.INTERIM_AVAILABLE)
                                        .build()
                        ))
                        .build())
                .transactions(account2Transactions())
                .build();
    }

    private List<ProviderTransactionDTO> account1Transactions() {
        return List.of(
                ProviderTransactionDTO.builder()
                        .dateTime(ZonedDateTime.of(2020, 10, 03, 00, 00, 00, 00, ZoneId.of("Europe/Rome")))
                        .amount(new BigDecimal("0.10"))
                        .status(BOOKED)
                        .type(CREDIT)
                        .description("Ref 12345678, Bestätigung der Transaktion: 23-02-2019 17:10:30")
                        .category(YoltCategory.GENERAL)
                        .extendedTransaction(ExtendedTransactionDTO.builder()
                                .status(BOOKED)
                                .bookingDate(ZonedDateTime.of(2020, 10, 03, 00, 00, 00, 00, ZoneId.of("Europe/Rome")))
                                .transactionAmount(BalanceAmountDTO.builder()
                                        .currency(CurrencyCode.EUR)
                                        .amount(new BigDecimal("0.10"))
                                        .build())
                                .creditorName("Deutsche Bank (IT) Current Account")
                                .creditorAccount(AccountReferenceDTO.builder()
                                        .type(AccountReferenceType.IBAN)
                                        .value("DE66500105172915693295")
                                        .build())
                                .remittanceInformationUnstructured("Ref 12345678, Bestätigung der Transaktion: 23-02-2019 17:10:30")
                                .transactionIdGenerated(true)
                                .build())
                        .build(),
                ProviderTransactionDTO.builder()
                        .dateTime(ZonedDateTime.of(2020, 10, 03, 00, 00, 00, 00, ZoneId.of("Europe/Rome")))
                        .amount(new BigDecimal("200.10"))
                        .status(BOOKED)
                        .type(CREDIT)
                        .description("Bestätigung der Transaktion: 23-02-2019 17:10:30")
                        .category(YoltCategory.GENERAL)
                        .extendedTransaction(ExtendedTransactionDTO.builder()
                                .status(BOOKED)
                                .bookingDate(ZonedDateTime.of(2020, 10, 03, 00, 00, 00, 00, ZoneId.of("Europe/Rome")))
                                .transactionAmount(BalanceAmountDTO.builder()
                                        .currency(CurrencyCode.EUR)
                                        .amount(new BigDecimal("200.10"))
                                        .build())
                                .creditorName("Deutsche Bank (IT) Current Account")
                                .creditorAccount(AccountReferenceDTO.builder()
                                        .type(AccountReferenceType.IBAN)
                                        .value("DE66500105172915693295")
                                        .build())
                                .remittanceInformationUnstructured("Bestätigung der Transaktion: 23-02-2019 17:10:30")
                                .transactionIdGenerated(true)
                                .build())
                        .build(),
                ProviderTransactionDTO.builder()
                        .dateTime(ZonedDateTime.of(2020, 10, 03, 00, 00, 00, 00, ZoneId.of("Europe/Rome")))
                        .amount(new BigDecimal("0.20"))
                        .status(BOOKED)
                        .type(DEBIT)
                        .description("Ref 12345678, Eine Beschreibung")
                        .category(YoltCategory.GENERAL)
                        .extendedTransaction(ExtendedTransactionDTO.builder()
                                .status(BOOKED)
                                .bookingDate(ZonedDateTime.of(2020, 10, 03, 00, 00, 00, 00, ZoneId.of("Europe/Rome")))
                                .transactionAmount(BalanceAmountDTO.builder()
                                        .currency(CurrencyCode.EUR)
                                        .amount(new BigDecimal("-0.20"))
                                        .build())
                                .debtorName("Deutsche Bank (IT) Current Account")
                                .debtorAccount(AccountReferenceDTO.builder()
                                        .type(AccountReferenceType.IBAN)
                                        .value("DE66500105172915693295")
                                        .build())
                                .remittanceInformationUnstructured("Ref 12345678, Eine Beschreibung")
                                .transactionIdGenerated(true)
                                .build())
                        .build(),
                ProviderTransactionDTO.builder()
                        .dateTime(ZonedDateTime.of(2020, 10, 03, 00, 00, 00, 00, ZoneId.of("Europe/Rome")))
                        .amount(new BigDecimal("400.20"))
                        .status(BOOKED)
                        .type(DEBIT)
                        .description("Eine Beschreibung - Tranz: Nr card 9999XXXXXX9999. Date: 23-02-2019 17:10:30")
                        .category(YoltCategory.GENERAL)
                        .extendedTransaction(ExtendedTransactionDTO.builder()
                                .status(BOOKED)
                                .bookingDate(ZonedDateTime.of(2020, 10, 03, 00, 00, 00, 00, ZoneId.of("Europe/Rome")))
                                .transactionAmount(BalanceAmountDTO.builder()
                                        .currency(CurrencyCode.EUR)
                                        .amount(new BigDecimal("-400.20"))
                                        .build())
                                .debtorName("Deutsche Bank (IT) Current Account")
                                .debtorAccount(AccountReferenceDTO.builder()
                                        .type(AccountReferenceType.IBAN)
                                        .value("DE66500105172915693295")
                                        .build())
                                .remittanceInformationUnstructured("Eine Beschreibung - Tranz: Nr card 9999XXXXXX9999. Date: 23-02-2019 17:10:30")
                                .transactionIdGenerated(true)
                                .build())
                        .build()
        );
    }

    private List<ProviderTransactionDTO> account2Transactions() {
        return List.of(
                ProviderTransactionDTO.builder()
                        .dateTime(ZonedDateTime.of(2020, 10, 03, 00, 00, 00, 00, ZoneId.of("Europe/Rome")))
                        .amount(new BigDecimal("400.00"))
                        .status(BOOKED)
                        .type(DEBIT)
                        .description("Data_Ora: 23-02-2019 17:10:30")
                        .category(YoltCategory.GENERAL)
                        .extendedTransaction(ExtendedTransactionDTO.builder()
                                .status(BOOKED)
                                .bookingDate(ZonedDateTime.of(2020, 10, 03, 00, 00, 00, 00, ZoneId.of("Europe/Rome")))
                                .transactionAmount(BalanceAmountDTO.builder()
                                        .currency(CurrencyCode.EUR)
                                        .amount(new BigDecimal("-400.00"))
                                        .build())
                                .debtorName("Deutsche Bank (IT) Current Account")
                                .debtorAccount(AccountReferenceDTO.builder()
                                        .type(AccountReferenceType.IBAN)
                                        .value("DE97500105176141527356")
                                        .build())
                                .remittanceInformationUnstructured("Data_Ora: 23-02-2019 17:10:30")
                                .transactionIdGenerated(true)
                                .build())
                        .build(),
                ProviderTransactionDTO.builder()
                        .dateTime(ZonedDateTime.of(2020, 10, 03, 00, 00, 00, 00, ZoneId.of("Europe/Rome")))
                        .amount(new BigDecimal("150.00"))
                        .status(BOOKED)
                        .type(CREDIT)
                        .description("Bestätigung der Transaktion - Tranz: Nr card 9999XXXXXX9999")
                        .category(YoltCategory.GENERAL)
                        .extendedTransaction(ExtendedTransactionDTO.builder()
                                .status(BOOKED)
                                .bookingDate(ZonedDateTime.of(2020, 10, 03, 00, 00, 00, 00, ZoneId.of("Europe/Rome")))
                                .transactionAmount(BalanceAmountDTO.builder()
                                        .currency(CurrencyCode.EUR)
                                        .amount(new BigDecimal("150.00"))
                                        .build())
                                .creditorName("Deutsche Bank (IT) Current Account")
                                .creditorAccount(AccountReferenceDTO.builder()
                                        .type(AccountReferenceType.IBAN)
                                        .value("DE97500105176141527356")
                                        .build())
                                .remittanceInformationUnstructured("Bestätigung der Transaktion - Tranz: Nr card 9999XXXXXX9999")
                                .transactionIdGenerated(true)
                                .build())
                        .build()
        );
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
        valueMap.put("email", "user.name@domain.com");

        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.setValueMap(valueMap);
        return filledInUserSiteFormValues;
    }

    private static Date parseDate(String date) {
        return Date.from(LocalDate.parse(date).atStartOfDay().toInstant(UTC));
    }
}