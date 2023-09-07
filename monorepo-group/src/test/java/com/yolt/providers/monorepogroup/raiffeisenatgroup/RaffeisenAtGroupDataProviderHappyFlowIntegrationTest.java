package com.yolt.providers.monorepogroup.raiffeisenatgroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.Date;
import java.time.*;
import java.util.*;
import java.util.stream.Stream;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.monorepogroup.raiffeisenatgroup.common.RaiffeisenAtGroupAuthenticationMeans.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = RaiffeisenAtGroupTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/raiffeisenatgroup/v1/ais/happy-flow/", httpsPort = 0, port = 0)
@ActiveProfiles("raiffeisenatgroup")
class RaffeisenAtGroupDataProviderHappyFlowIntegrationTest {

    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final String STATE = "0fa8b824-fb95-11ec-b939-0242ac120002";
    private static final String KEY_ID_VALUE = "7a7251ff-45ef-4e24-a4cc-bb77d4ba0b16";
    private static final String REDIRECT_URI = "https://yolt.com/callback";
    private static final String CLIENT_ID = "API-7d6f204b-3219-4b63-b66e-5f7a0be1d067";
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Vienna");

    @Autowired
    @Qualifier("RaiffeisenAtGroupObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("RaiffeisenAtDataProviderV1")
    private UrlDataProvider raiffeisenAtDataProvider;

    @Autowired
    private Clock clock;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    private Stream<UrlDataProvider> getProviders() {
        return Stream.of(raiffeisenAtDataProvider);
    }

    private Stream<AutoOnboardingProvider> getProvidersForAutoonboarding() {
        return Stream.of((AutoOnboardingProvider) raiffeisenAtDataProvider);
    }

    @BeforeEach
    void initialize() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:certificates/fake-certificate.pem");
        String pemCertificate = String.join("\n", Files.readAllLines(resource.getFile().toPath(), UTF_8));

        authenticationMeans = new HashMap<>();
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), pemCertificate));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), KEY_ID_VALUE));
        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID));
    }

    @ParameterizedTest
    @MethodSource("getProvidersForAutoonboarding")
    void shouldRegisterProvider(AutoOnboardingProvider provider) {
        //given
        Map<String, BasicAuthenticationMean> authenticationMeansForRegistration = new HashMap<>(authenticationMeans);
        authenticationMeansForRegistration.remove(CLIENT_ID);
        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(authenticationMeansForRegistration)
                .setRestTemplateManager(restTemplateManager)
                .build();
        Map<String, BasicAuthenticationMean> expectedAuthenticationMeans = new HashMap<>(authenticationMeansForRegistration);
        expectedAuthenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), "API-REGISTRATION-SUCCESS"));

        //when
        Map<String, BasicAuthenticationMean> result = provider.autoConfigureMeans(urlAutoOnboardingRequest);

        //then
        assertThat(result).isEqualTo(expectedAuthenticationMeans);

    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnTypedAuthenticationMeans(UrlDataProvider dataProvider) {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = dataProvider.getTypedAuthenticationMeans();

        // then
        assertThat(typedAuthMeans).containsAllEntriesOf(Map.of(CLIENT_ID_NAME, CLIENT_ID_STRING, TRANSPORT_CERTIFICATE_ID_NAME, KEY_ID, TRANSPORT_CERTIFICATE_NAME, CERTIFICATE_PEM));
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnRedirectStep(UrlDataProvider dataProvider) {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setState(STATE)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .build();

        // when
        var redirectStep = (RedirectStep) dataProvider.getLoginInfo(request);

        // then
        UriComponents loginUrl = UriComponentsBuilder.fromHttpUrl(redirectStep.getRedirectUrl()).build();
        assertThat(loginUrl).satisfies(url -> {
                    assertThat(url.getScheme()).isEqualTo("https");
                    assertThat(url.getHost()).isEqualTo("sandbox.raiffeisen.at");
                    assertThat(url.getPath()).isEqualTo("/psd2-sandbox-ui/sandbox-ui/");
                    assertThat(url.getQueryParams().toSingleValueMap()).containsAllEntriesOf(
                            Map.of("consent_id", "8edfea3a-a660-11eb-bcbc-0242ac130002",
                                    "redirectBackUrl", "https%3A%2F%2Fwww.yolt.com%2Fcallback%3Fstate%3D0fa8b824-fb95-11ec-b939-0242ac120002")
                    );
                }
        );
        assertThat(redirectStep.getExternalConsentId()).isEqualTo("8edfea3a-a660-11eb-bcbc-0242ac130002");
        assertThat(redirectStep.getProviderState()).isEqualTo("{\"consentId\":\"8edfea3a-a660-11eb-bcbc-0242ac130002\"}");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnNewAccessMeans(UrlDataProvider dataProvider) {
        // given
        var request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setBaseClientRedirectUrl(REDIRECT_URI)
                .setState(STATE)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setProviderState("{\"consentId\":\"8edfea3a-a660-11eb-bcbc-0242ac130002\"}")
                .setRedirectUrlPostedBackFromSite("https://yolt.com/callback?state=fa8b824-fb95-11ec-b939-0242ac120002")
                .setUserId(USER_ID)
                .build();
        var expectedAccessMeansOrState = new AccessMeansOrStepDTO(
                new AccessMeansDTO(
                        USER_ID,
                        "{\"consentId\":\"8edfea3a-a660-11eb-bcbc-0242ac130002\"}",
                        Date.from(LocalDate.now(clock).atStartOfDay(ZONE_ID).toInstant()),
                        Date.from(LocalDate.of(2022, 06, 27).atStartOfDay(ZONE_ID).toInstant())
                )
        );

        // when
        var result = dataProvider.createNewAccessMeans(request);

        //then
        assertThat(result).isEqualTo(expectedAccessMeansOrState);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldDeleteUserConsent(UrlDataProvider dataProvider) {
        //given
        var request = new UrlOnUserSiteDeleteRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setExternalConsentId("8edfea3a-a660-11eb-bcbc-0242ac130002")
                .build();

        //when
        ThrowableAssert.ThrowingCallable call = () -> dataProvider.onUserSiteDelete(request);

        //then
        assertThatCode(call)
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldFetchData(UrlDataProvider dataProvider) throws TokenInvalidException, ProviderFetchDataException, JsonProcessingException {
        // given
        var accessMeans = new AccessMeansDTO(USER_ID,
                "{\"consentId\":\"8edfea3a-a660-11eb-bcbc-0242ac130002\"}",
                java.util.Date.from(LocalDate.now(clock).atStartOfDay(ZONE_ID).toInstant()),
                java.util.Date.from(LocalDate.now(clock).plusDays(89).atStartOfDay(ZONE_ID).toInstant()));

        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setAccessMeans(accessMeans)
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setTransactionsFetchStartTime(Instant.now(clock))
                .build();
        DataProviderResponse expectedResponse = createExpectedResponse();

        // when
        var response = dataProvider.fetchData(request);

        // then
        assertThat(response).isEqualTo(expectedResponse);
    }

    private DataProviderResponse createExpectedResponse() {
        return new DataProviderResponse(List.of(createAccount1(), createAccount2()));
    }

    private ProviderAccountDTO createAccount1() {
        return ProviderAccountDTO.builder()
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(new BigDecimal("1340.55"))
                .currentBalance(new BigDecimal("2850.49"))
                .accountId("AT099900000000001511")
                .accountNumber(new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, "AT099900000000001511"))
                .bic("TESTAT99")
                .name("Marianne Mustermann")
                .currency(CurrencyCode.EUR)
                .transactions(List.of(createTransaction1ForAccount1(), createTransaction2ForAccount1()))
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("AT099900000000001511")
                        .accountReferences(Collections.singletonList(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("AT099900000000001511")
                                .build()))
                        .currency(CurrencyCode.EUR)
                        .name("Marianne Mustermann")
                        .product("Giro")
                        .cashAccountType(ExternalCashAccountType.CURRENT)
                        .bic("TESTAT99")
                        .balances(List.of(BalanceDTO.builder()
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .currency(CurrencyCode.EUR)
                                                .amount(new BigDecimal("1340.55"))
                                                .build())
                                        .balanceType(BalanceType.FORWARD_AVAILABLE)
                                        .referenceDate(LocalDate.of(2022, 07, 06).atStartOfDay(ZONE_ID))
                                        .build(),
                                BalanceDTO.builder()
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .currency(CurrencyCode.EUR)
                                                .amount(new BigDecimal("2850.49"))
                                                .build())
                                        .balanceType(BalanceType.INTERIM_AVAILABLE)
                                        .referenceDate(LocalDate.of(2022, 07, 05).atStartOfDay(ZONE_ID))
                                        .build()))
                        .build())
                .build();
    }

    private ProviderTransactionDTO createTransaction1ForAccount1() {
        return ProviderTransactionDTO.builder()
                .externalId("22123461")
                .dateTime(LocalDate.of(2022, 07, 05).atStartOfDay(ZONE_ID))
                .amount(new BigDecimal("1533.11"))
                .status(TransactionStatus.BOOKED)
                .type(ProviderTransactionType.DEBIT)
                .description("Salary")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(TransactionStatus.BOOKED)
                        .bookingDate(LocalDate.of(2022, 07, 05).atStartOfDay(ZONE_ID))
                        .valueDate(LocalDate.of(2022, 07, 06).atStartOfDay(ZONE_ID))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .currency(CurrencyCode.EUR)
                                .amount(new BigDecimal("-1533.11"))
                                .build())
                        .creditorName("Georg Mustermann")
                        .creditorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("AT439900000000010017")
                                .build())
                        .debtorName("Marianne Mustermann")
                        .debtorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("AT099900000000001511")
                                .build())
                        .remittanceInformationUnstructured("Salary")
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }

    private ProviderTransactionDTO createTransaction2ForAccount1() {
        return ProviderTransactionDTO.builder()
                .externalId("22123456")
                .dateTime(LocalDate.of(2022, 07, 05).atStartOfDay(ZONE_ID))
                .amount(new BigDecimal("789.89"))
                .status(TransactionStatus.PENDING)
                .type(ProviderTransactionType.CREDIT)
                .description("Pending Order Sent")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(TransactionStatus.PENDING)
                        .bookingDate(LocalDate.of(2022, 07, 05).atStartOfDay(ZONE_ID))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .currency(CurrencyCode.EUR)
                                .amount(new BigDecimal("789.89"))
                                .build())
                        .creditorName("Pending Order")
                        .creditorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("AT439900000000010017")
                                .build())
                        .debtorName("DebtorName1")
                        .debtorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("AT099900000000001511")
                                .build())
                        .remittanceInformationUnstructured("Pending Order Sent")
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }

    private ProviderAccountDTO createAccount2() {
        return ProviderAccountDTO.builder()
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(new BigDecimal("4533.55"))
                .currentBalance(new BigDecimal("5119.49"))
                .accountId("AT439900000000010017")
                .accountNumber(new ProviderAccountNumberDTO(ProviderAccountNumberDTO.Scheme.IBAN, "AT439900000000010017"))
                .bic("TESTAT99")
                .name("Georg Mustermann")
                .currency(CurrencyCode.EUR)
                .transactions(List.of(createTransaction1ForAccount2(), createTransaction2ForAccount2()))
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("AT439900000000010017")
                        .accountReferences(Collections.singletonList(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("AT439900000000010017")
                                .build()))
                        .currency(CurrencyCode.EUR)
                        .name("Georg Mustermann")
                        .product("Giro")
                        .cashAccountType(ExternalCashAccountType.CURRENT)
                        .bic("TESTAT99")
                        .balances(List.of(BalanceDTO.builder()
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .currency(CurrencyCode.EUR)
                                                .amount(new BigDecimal("4533.55"))
                                                .build())
                                        .balanceType(BalanceType.FORWARD_AVAILABLE)
                                        .referenceDate(LocalDate.of(2022, 07, 06).atStartOfDay(ZONE_ID))
                                        .build(),
                                BalanceDTO.builder()
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .currency(CurrencyCode.EUR)
                                                .amount(new BigDecimal("5119.49"))
                                                .build())
                                        .balanceType(BalanceType.INTERIM_AVAILABLE)
                                        .referenceDate(LocalDate.of(2022, 07, 05).atStartOfDay(ZONE_ID))
                                        .build()))
                        .build())
                .build();
    }

    private ProviderTransactionDTO createTransaction1ForAccount2() {
        return ProviderTransactionDTO.builder()
                .externalId("34123456")
                .dateTime(LocalDate.of(2022, 07, 05).atStartOfDay(ZONE_ID))
                .amount(new BigDecimal("9820.99"))
                .status(TransactionStatus.BOOKED)
                .type(ProviderTransactionType.CREDIT)
                .description("incoming payment")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(TransactionStatus.BOOKED)
                        .bookingDate(LocalDate.of(2022, 07, 05).atStartOfDay(ZONE_ID))
                        .valueDate(LocalDate.of(2022, 07, 06).atStartOfDay(ZONE_ID))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .currency(CurrencyCode.EUR)
                                .amount(new BigDecimal("9820.99"))
                                .build())
                        .creditorName("Marianne Mustermann")
                        .creditorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("AT099900000000001511")
                                .build())
                        .debtorName("DebtorName1")
                        .debtorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("AT439900000000010017")
                                .build())
                        .remittanceInformationUnstructured("incoming payment")
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }

    private ProviderTransactionDTO createTransaction2ForAccount2() {
        return ProviderTransactionDTO.builder()
                .externalId("34123458")
                .dateTime(LocalDate.of(2022, 07, 05).atStartOfDay(ZONE_ID))
                .amount(new BigDecimal("5988.00"))
                .status(TransactionStatus.BOOKED)
                .type(ProviderTransactionType.DEBIT)
                .description("Payment")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(TransactionStatus.BOOKED)
                        .bookingDate(LocalDate.of(2022, 07, 05).atStartOfDay(ZONE_ID))
                        .valueDate(LocalDate.of(2022, 07, 06).atStartOfDay(ZONE_ID))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .currency(CurrencyCode.EUR)
                                .amount(new BigDecimal("-5988.00"))
                                .build())
                        .creditorName("Marianne Mustermann")
                        .creditorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("AT099900000000001511")
                                .build())
                        .debtorName("DebtorName2")
                        .debtorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("AT439900000000010017")
                                .build())
                        .remittanceInformationUnstructured("Payment")
                        .transactionIdGenerated(true)
                        .build())
                .build();
    }
}