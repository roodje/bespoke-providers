package com.yolt.providers.monorepogroup.olbgroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.form.EncryptionDetails;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.olbgroup.common.OlbGroupDataProvider;
import com.yolt.providers.monorepogroup.olbgroup.common.domain.OlbGroupProviderState;
import com.yolt.providers.monorepogroup.olbgroup.common.mapper.OlbGroupProviderStateMapper;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import nl.ing.lovebird.providershared.form.Form;
import nl.ing.lovebird.providershared.form.TextField;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import java.time.*;
import java.util.*;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CERTIFICATE_PEM;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.KEY_ID;
import static com.yolt.providers.monorepogroup.olbgroup.common.auth.OlbGroupAuthenticationMeansProducerV1.TRANSPORT_CERTIFICATE_NAME;
import static com.yolt.providers.monorepogroup.olbgroup.common.auth.OlbGroupAuthenticationMeansProducerV1.TRANSPORT_KEY_ID_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.temporal.ChronoUnit.HOURS;
import static nl.ing.lovebird.extendeddata.account.ExternalCashAccountType.CURRENT;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.PENDING;
import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.IBAN;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = OlbGroupTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/olbgroup/ais/happy-flow/", httpsPort = 0, port = 0)
@ActiveProfiles("olbgroup")
class OlbGroupDataProviderHappyFlowIntegrationTest {

    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final String STATE = "7c3e98de-0239-4868-ada8-aefb5384ef0a";
    private static final String CONSENT_ID = "7a7251ff-45ef-4e24-a4cc-bb77d4ba0b16";
    private static final String KEY_ID_VALUE = "7a7251ff-45ef-4e24-a4cc-bb77d4ba0b16";
    private static final String REDIRECT_URI = "https://yolt.com/callback";
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Berlin");
    private static final String CONSENT_URL = "https://xs2a.olb.de/xs2a/OLB/authorize";

    @Autowired
    @Qualifier("OlbGroupObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("OlbDataProviderV1")
    private OlbGroupDataProvider dataProvider;

    @Autowired
    private Clock clock;

    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private OlbGroupProviderStateMapper providerStateMapper;

    @BeforeEach
    void initialize() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:certificates/fake-certificate.pem");
        String pemCertificate = String.join("\n", Files.readAllLines(resource.getFile().toPath(), UTF_8));

        authenticationMeans = new HashMap<>();
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), pemCertificate));
        authenticationMeans.put(TRANSPORT_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), KEY_ID_VALUE));

        providerStateMapper = new OlbGroupProviderStateMapper(objectMapper);
    }

    @Test
    void shouldReturnTypedAuthenticationMeans() {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthMeans).containsAllEntriesOf(Map.of(TRANSPORT_KEY_ID_NAME, KEY_ID, TRANSPORT_CERTIFICATE_NAME, CERTIFICATE_PEM));
    }

    @Test
    void shouldReturnFormStepWithUserIDField() {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setState(STATE)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .build();

        // when
        FormStep formStep = dataProvider.getLoginInfo(request);

        // then
        assertThat(formStep).isEqualTo(createFormStep());
    }

    @Test
    void shouldReturnRedirectStep() {
        // given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .setState(STATE)
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
    void shouldReturnNewAccessMeans() {
        // given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .setState(STATE)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setProviderState(providerStateMapper.toJson(createProviderState()))
                .setRedirectUrlPostedBackFromSite("https://yolt.com/callback?state=29dbba15-1e67-4ac0-ab0f-2487dc0c960b&code=authorization-code")
                .setUserId(USER_ID)
                .build();

        // when
        var accessMeansOrStepDTO = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(accessMeansOrStepDTO).isEqualTo(createAccessMeansOrStepDTO());
    }

    @Test
    void shouldReturnTokenInvalidExceptionDuringRefreshAccessMeans() {
        // given
        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setAccessMeans(createAccessMeansDTO())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .build();

        // when
        ThrowableAssert.ThrowingCallable refreshAccessMeansCallable = () -> dataProvider.refreshAccessMeans(request);

        // then
        assertThatThrownBy(refreshAccessMeansCallable)
                .isExactlyInstanceOf(TokenInvalidException.class)
                .hasMessage("Refresh token flow is not supported");
    }

    @Test
    void shouldDeleteConsent() {
        // given
        UrlOnUserSiteDeleteRequest onUserSiteDeleteRequest = new UrlOnUserSiteDeleteRequestBuilder()
                .setAccessMeans(createAccessMeansDTO())
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setExternalConsentId(CONSENT_ID)
                .build();
        // when
        ThrowableAssert.ThrowingCallable onUserSiteDeleteCallable = () -> dataProvider.onUserSiteDelete(onUserSiteDeleteRequest);

        // then
        assertThatNoException().isThrownBy(onUserSiteDeleteCallable);
    }

    @Test
    void shouldFetchData() throws TokenInvalidException, ProviderFetchDataException, JsonProcessingException {
        // given
        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setAccessMeans(createAccessMeansDTO())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setTransactionsFetchStartTime(Instant.now())
                .build();
        DataProviderResponse expectedResponse = createExpectedResponse();

        // when
        DataProviderResponse response = dataProvider.fetchData(request);

        // then
        response.getAccounts().forEach(ProviderAccountDTO::validate);
        assertThat(response).isEqualTo(expectedResponse);
    }


    private AccessMeansOrStepDTO createAccessMeansOrStepDTO() {
        return new AccessMeansOrStepDTO(createAccessMeansDTO());
    }

    private AccessMeansDTO createAccessMeansDTO() {
        OlbGroupProviderState providerState = createProviderState();
        return new AccessMeansDTO(USER_ID,
                providerStateMapper.toJson(providerState),
                Date.from(LocalDate.now(clock).atStartOfDay(ZONE_ID).toInstant()),
                Date.from(LocalDate.now(clock).plusDays(89).atStartOfDay(ZONE_ID).toInstant()));
    }

    private OlbGroupProviderState createProviderState() {
        return new OlbGroupProviderState(CONSENT_ID);
    }

    private Map<String, String> getQueryParamsFromUrl(String url) {
        return UriComponentsBuilder.fromUriString(url).build()
                .getQueryParams()
                .toSingleValueMap();
    }

    private FormStep createFormStep() {
        var usernameField = new TextField("username", "Username", 0, 255, false, false);
        var form = new Form();
        form.setFormComponents(Collections.singletonList(usernameField));
        return new FormStep(form, EncryptionDetails.noEncryption(), Instant.now(clock).plus(1L, HOURS), null);
    }

    private FilledInUserSiteFormValues createFilledInUserSiteFormValues() {
        HashMap<String, String> valueMap = new HashMap<>(1);
        valueMap.put("username", "John");

        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.setValueMap(valueMap);
        return filledInUserSiteFormValues;
    }

    private ZonedDateTime toZonedTime(int year, int month, int day) {
        return ZonedDateTime.of(LocalDate.of(year, month, day), LocalTime.MIN, ZoneId.of(("Europe/Berlin")));
    }

    private DataProviderResponse createExpectedResponse() {
        return new DataProviderResponse(
                List.of(createAccount1(), createAccount2())
        );
    }

    private ProviderAccountDTO createAccount1() {
        return ProviderAccountDTO.builder()
                .accountId("1")
                .accountNumber(new ProviderAccountNumberDTO(IBAN, "DE66500105172915693295"))
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(new BigDecimal("510.10"))
                .currentBalance(new BigDecimal("510.10"))
                .currency(CurrencyCode.EUR)
                .name("Oldenburgische Landesbank Current Account")
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .transactions(List.of(
                        createAccount1Transaction1(),
                        createAccount1Transaction2(),
                        createAccount1Transaction3()
                ))
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("1")
                        .accountReferences(List.of(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("DE66500105172915693295")
                                .build()))
                        .currency(CurrencyCode.EUR)
                        .name("Oldenburgische Landesbank Current Account")
                        .cashAccountType(CURRENT)
                        .balances(List.of(
                                BalanceDTO.builder()
                                        .balanceType(BalanceType.INTERIM_AVAILABLE)
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(new BigDecimal("510.10"))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .referenceDate(toZonedTime(2019, 2, 28))
                                        .build()
                        ))
                        .build())
                .build();
    }

    private ProviderTransactionDTO createAccount1Transaction1() {
        return ProviderTransactionDTO.builder()
                .externalId("313ATCW1823600GV")
                .dateTime(toZonedTime(2020, 10, 3))
                .amount(new BigDecimal("0.10"))
                .status(BOOKED)
                .type(CREDIT)
                .description("N/A")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .bookingDate(toZonedTime(2020, 10, 3))
                        .valueDate(toZonedTime(2020, 10, 3))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(new BigDecimal("0.10"))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .creditorName("Ms Monica")
                        .creditorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("DE13500105173668147693")
                                .build())
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }

    private ProviderTransactionDTO createAccount1Transaction2() {
        return ProviderTransactionDTO.builder()
                .externalId("766ATCW18223009Z")
                .dateTime(toZonedTime(2020, 10, 3))
                .amount(new BigDecimal("200.10"))
                .status(BOOKED)
                .type(CREDIT)
                .description("N/A")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .bookingDate(toZonedTime(2020, 10, 3))
                        .valueDate(toZonedTime(2020, 10, 3))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(new BigDecimal("200.10"))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .debtorName("Mr Gerald")
                        .debtorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("DE05500105177645384711")
                                .build())
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }

    private ProviderTransactionDTO createAccount1Transaction3() {
        return ProviderTransactionDTO.builder()
                .externalId("013ATCW1822203ZV")
                .dateTime(toZonedTime(2020, 10, 3))
                .amount(new BigDecimal("0.20"))
                .status(BOOKED)
                .type(DEBIT)
                .description("N/A")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .bookingDate(toZonedTime(2020, 10, 3))
                        .valueDate(toZonedTime(2020, 10, 3))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(new BigDecimal("-0.20"))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .creditorName("Ms Monica")
                        .creditorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("DE98500105176148563427")
                                .build())
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }

    private ProviderAccountDTO createAccount2() {
        return ProviderAccountDTO.builder()
                .accountId("2")
                .accountNumber(new ProviderAccountNumberDTO(IBAN, "DE97500105176141527356"))
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(new BigDecimal("-20.10"))
                .currentBalance(new BigDecimal("210.10"))
                .currency(CurrencyCode.EUR)
                .name("Oldenburgische Landesbank Current Account")
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .transactions(List.of(
                        createAccount2Transaction1(),
                        createAccount2Transaction2()
                ))
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("2")
                        .accountReferences(List.of(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("DE97500105176141527356")
                                .build()))
                        .currency(CurrencyCode.EUR)
                        .name("Oldenburgische Landesbank Current Account")
                        .cashAccountType(CURRENT)
                        .balances(List.of(
                                BalanceDTO.builder()
                                        .balanceType(BalanceType.CLOSING_BOOKED)
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(new BigDecimal("210.10"))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .build(),
                                BalanceDTO.builder()
                                        .balanceType(BalanceType.INTERIM_AVAILABLE)
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(new BigDecimal("-20.10"))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .build()
                        ))
                        .build())
                .build();
    }

    private ProviderTransactionDTO createAccount2Transaction1() {
        return ProviderTransactionDTO.builder()
                .externalId("011EACH182320054")
                .dateTime(toZonedTime(2020, 10, 3))
                .amount(new BigDecimal("0.13"))
                .status(PENDING)
                .type(DEBIT)
                .description("Ref 12345678, Suma platita 252.91 EUR")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(PENDING)
                        .bookingDate(toZonedTime(2020, 10, 3))
                        .valueDate(toZonedTime(2020, 10, 3))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(new BigDecimal("-0.13"))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .debtorName("Mr Edward")
                        .debtorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("DE74500105174697811586")
                                .build())
                        .remittanceInformationUnstructured("Ref 12345678, Suma platita 252.91 EUR")
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }

    private ProviderTransactionDTO createAccount2Transaction2() {
        return ProviderTransactionDTO.builder()
                .externalId("328CHDP182190065")
                .dateTime(toZonedTime(2020, 10, 3))
                .amount(new BigDecimal("400.00"))
                .status(BOOKED)
                .type(DEBIT)
                .description("Data_Ora: 23-02-2019 17:10:30")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .bookingDate(toZonedTime(2020, 10, 3))
                        .valueDate(toZonedTime(2020, 10, 3))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(new BigDecimal("-400.00"))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .debtorName("Ms Alexa")
                        .debtorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("DE88500105179169635979")
                                .build())
                        .remittanceInformationUnstructured("Data_Ora: 23-02-2019 17:10:30")
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }
}