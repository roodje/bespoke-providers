package com.yolt.providers.monorepogroup.libragroup.common.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.TestSigner;
import com.yolt.providers.monorepogroup.libragroup.LibraGroupTestApp;
import com.yolt.providers.monorepogroup.libragroup.common.LibraGroupDataProvider;
import com.yolt.providers.monorepogroup.libragroup.common.LibraGroupSampleAuthenticationMeans;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto.LibraGroupAccessMeans;
import com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto.TestLibraGroupTokens;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.UsageType;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
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
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static nl.ing.lovebird.extendeddata.account.ExternalCashAccountType.CURRENT;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.IBAN;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = LibraGroupTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/libragroup/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("libragroup")
class LibraGroupDataProviderV1IntegrationTest {

    private static final String TEST_PSU_IP_ADDRESS = "123.45.67.89";
    private static final String CONSENT_ID = "VALID_CONSENT_ID";
    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String REDIRECT_URL = "https://yolt.com/callback-acc";
    private static final int ONE_HOUR = 3600000;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("LibraDataProviderV1")
    private LibraGroupDataProvider dataProvider;

    @Autowired
    private Clock clock;

    @Autowired
    @Qualifier("LibraGroupObjectMapper")
    private ObjectMapper objectMapper;

    private TestSigner signer;

    public Stream<LibraGroupDataProvider> getDataProviders() {
        return Stream.of(dataProvider);
    }

    @BeforeEach
    public void setup() throws IOException, URISyntaxException {
        authenticationMeans = new LibraGroupSampleAuthenticationMeans().getAuthenticationMeans();
        signer = new TestSigner();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnRedirectStepOnGetLoginInfo(LibraGroupDataProvider dataProvider) {
        // given
        String state = UUID.randomUUID().toString();
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(state)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .build();

        // when
        RedirectStep step = dataProvider.getLoginInfo(urlGetLogin);

        // then

        assertThat(step).isEqualTo(new RedirectStep("https://localhost/oAuth?" +
                "scope=AIS:consentId&" +
                "response_type=code&" +
                "redirect_uri=https%3A%2F%2Fyolt.com%2Fcallback-acc&" +
                "client_id=clientId&" +
                "state=" + state,
                "consentId",
                "consentId"));
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldCreateNewAccessMeans(LibraGroupDataProvider dataProvider) throws JsonProcessingException {
        // given
        String redirectUrlWithCode = REDIRECT_URL + "?code=test-code";
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setUserId(TEST_USER_ID)
                .setRedirectUrlPostedBackFromSite(redirectUrlWithCode)
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setProviderState(CONSENT_ID)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .build();
        String expectedAccessMeans = objectMapper.writeValueAsString(new LibraGroupAccessMeans(
                new TestLibraGroupTokens(
                        "new_access_token",
                        1640998800000L,
                        "new_refresh_token"),
                REDIRECT_URL,
                CONSENT_ID)
        );

        // when
        AccessMeansOrStepDTO result = dataProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThat(result).isEqualTo(new AccessMeansOrStepDTO(
                new AccessMeansDTO(
                        TEST_USER_ID,
                        expectedAccessMeans,
                        new Date(clock.millis()),
                        new Date(clock.millis() + ONE_HOUR)))
        );
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldRefreshTokenSuccessfully(LibraGroupDataProvider dataProvider) throws JsonProcessingException, TokenInvalidException {
        // given
        UrlRefreshAccessMeansRequest refreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(getAccessMeans())
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .build();
        String expectedAccessMeans = objectMapper.writeValueAsString(new LibraGroupAccessMeans(
                new TestLibraGroupTokens(
                        "new_access_token",
                        1640998800000L,
                        "new_refresh_token"),
                REDIRECT_URL,
                CONSENT_ID)
        );
        // when
        AccessMeansDTO result = dataProvider.refreshAccessMeans(refreshAccessMeansRequest);

        // then
        assertThat(result).isEqualTo(new AccessMeansDTO(
                TEST_USER_ID,
                expectedAccessMeans,
                new Date(clock.millis()),
                new Date(clock.millis() + ONE_HOUR))
        );
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldDeleteConsentSuccessfully(LibraGroupDataProvider dataProvider) throws JsonProcessingException {
        // given
        UrlOnUserSiteDeleteRequest onUserSiteDeleteRequest = new UrlOnUserSiteDeleteRequestBuilder()
                .setAccessMeans(getAccessMeans())
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setExternalConsentId(CONSENT_ID)
                .setSigner(signer)
                .build();
        // when
        ThrowableAssert.ThrowingCallable onUserSiteDeleteCallable = () -> dataProvider.onUserSiteDelete(onUserSiteDeleteRequest);

        // then
        assertThatNoException().isThrownBy(onUserSiteDeleteCallable);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldFetchDataSuccessfully(LibraGroupDataProvider subject) throws TokenInvalidException, ProviderFetchDataException, JsonProcessingException {
        // given
        UrlFetchDataRequest urlFetchDataRequest = new UrlFetchDataRequestBuilder()
                .setAccessMeans(getAccessMeans())
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setTransactionsFetchStartTime(Instant.now(clock))
                .setUserId(TEST_USER_ID)
                .setSigner(signer)
                .build();
        DataProviderResponse expectedResponse = createExpectedResponse();

        // when
        DataProviderResponse dataProviderResponse = subject.fetchData(urlFetchDataRequest);

        // then
        assertThat(dataProviderResponse).usingRecursiveComparison()
                .isEqualTo(expectedResponse);
    }

    private AccessMeansDTO getAccessMeans() throws JsonProcessingException {
        LibraGroupAccessMeans tokens = new LibraGroupAccessMeans(
                new TestLibraGroupTokens(
                        "access_token",
                        1003600000L,
                        "refresh_token"),
                REDIRECT_URL,
                CONSENT_ID);

        return new AccessMeansDTO(TEST_USER_ID,
                objectMapper.writeValueAsString(tokens),
                new Date(clock.millis()),
                new Date(clock.millis()));
    }

    private ZonedDateTime toZonedTime(int year, int month, int day) {
        return ZonedDateTime.of(LocalDate.of(year, month, day), LocalTime.MIN, ZoneId.of(("Europe/Brussels")));
    }

    private ZonedDateTime toZonedTime(int year, int month, int day, int hour, int min, int second) {
        return ZonedDateTime.of(LocalDate.of(year, month, day), LocalTime.of(hour, min, second), ZoneId.of(("Europe/Brussels")));
    }

    private DataProviderResponse createExpectedResponse() {
        return new DataProviderResponse(
                List.of(account0(), account1())
        );
    }

    private ProviderAccountDTO account0() {
        ProviderAccountNumberDTO accountNumber_account1 = new ProviderAccountNumberDTO(IBAN, "RO21BREL2222222222220100");
        accountNumber_account1.setHolderName("FIN2222 SRL");
        return ProviderAccountDTO.builder()
                .accountId("2002222222100")
                .name("Cont Curent Persoane Juri")
                .currency(CurrencyCode.RON)
                .accountNumber(accountNumber_account1)
                .currentBalance(BigDecimal.valueOf(6428.51))
                .availableBalance(BigDecimal.valueOf(6338.10))
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .lastRefreshed(ZonedDateTime.now(clock))
                .transactions(List.of(
                        account0Transaction0(),
                        account0Transaction1()))
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("2002222222100")
                        .accountReferences(List.of(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("RO21BREL2222222222220100")
                                .build()))
                        .currency(CurrencyCode.RON)
                        .name("Cont Curent Persoane Juri")
                        .cashAccountType(CURRENT)
                        .usage(UsageType.PRIVATE)
                        .balances(List.of(
                                BalanceDTO.builder()
                                        .balanceType(BalanceType.EXPECTED)
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(BigDecimal.valueOf(1450))
                                                .currency(CurrencyCode.RON)
                                                .build())
                                        .referenceDate(toZonedTime(2020, 7, 21))
                                        .lastChangeDateTime(toZonedTime(2020, 7, 21, 3, 59, 51))
                                        .build()
                        ))
                        .build())
                .build();
    }

    private ProviderTransactionDTO account0Transaction0() {
        return ProviderTransactionDTO.builder()
                .externalId("FT20155HG4CR1")
                .dateTime(toZonedTime(2020, 6, 2))
                .amount(BigDecimal.valueOf(50001))
                .status(BOOKED)
                .type(CREDIT)
                .description("11 from: a")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .entryReference("FT20155HG4CR1")
                        .bookingDate(toZonedTime(2020, 6, 1))
                        .valueDate(toZonedTime(2020, 6, 2))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(BigDecimal.valueOf(50001))
                                .currency(CurrencyCode.RON)
                                .build())
                        .debtorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("RO60BREL0002002107930101")
                                .build())
                        .debtorName("a")
                        .remittanceInformationUnstructured("11 from: a")
                        .transactionIdGenerated(false)
                        .build())
                .build();
    }

    private ProviderTransactionDTO account0Transaction1() {
        return ProviderTransactionDTO.builder()
                .externalId("FT20155HG4CR2")
                .dateTime(toZonedTime(2020, 6, 3))
                .amount(BigDecimal.valueOf(50002))
                .status(BOOKED)
                .type(CREDIT)
                .description("12 from: a")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .entryReference("FT20155HG4CR2")
                        .bookingDate(toZonedTime(2020, 6, 2))
                        .valueDate(toZonedTime(2020, 6, 3))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(BigDecimal.valueOf(50002))
                                .currency(CurrencyCode.RON)
                                .build())
                        .debtorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("RO60BREL0002002107930102")
                                .build())
                        .debtorName("a")
                        .remittanceInformationUnstructured("12 from: a")
                        .transactionIdGenerated(false)
                        .build())
                .build();
    }

    private ProviderAccountDTO account1() {
        ProviderAccountNumberDTO accountNumber_account1 = new ProviderAccountNumberDTO(IBAN, "RO21BREL2222222222220103");
        accountNumber_account1.setHolderName("FIN2222 SRL3");
        return ProviderAccountDTO.builder()
                .accountId("2002222222103")
                .name("Cont Curent Persoane Juri3")
                .currency(CurrencyCode.RON)
                .accountNumber(accountNumber_account1)
                .currentBalance(BigDecimal.valueOf(68.53))
                .availableBalance(BigDecimal.valueOf(68.13))
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .lastRefreshed(ZonedDateTime.now(clock))
                .transactions(List.of(
                        account1Transaction0()))
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("2002222222103")
                        .accountReferences(List.of(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("RO21BREL2222222222220103")
                                .build()))
                        .currency(CurrencyCode.RON)
                        .name("Cont Curent Persoane Juri3")
                        .cashAccountType(CURRENT)
                        .balances(List.of(
                                BalanceDTO.builder()
                                        .balanceType(BalanceType.EXPECTED)
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(BigDecimal.valueOf(1450))
                                                .currency(CurrencyCode.RON)
                                                .build())
                                        .referenceDate(toZonedTime(2020, 7, 21))
                                        .lastChangeDateTime(toZonedTime(2020, 7, 21, 3, 59, 51))
                                        .build()
                        ))
                        .build())
                .build();
    }

    private ProviderTransactionDTO account1Transaction0() {
        return ProviderTransactionDTO.builder()
                .externalId("FT20155HG4CR3")
                .dateTime(toZonedTime(2020, 6, 4))
                .amount(BigDecimal.valueOf(50003))
                .status(BOOKED)
                .type(DEBIT)
                .description("13 from: a")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .entryReference("FT20155HG4CR3")
                        .bookingDate(toZonedTime(2020, 6, 3))
                        .valueDate(toZonedTime(2020, 6, 4))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(BigDecimal.valueOf(-50003))
                                .currency(CurrencyCode.RON)
                                .build())
                        .debtorName("a")
                        .debtorAccount(AccountReferenceDTO.builder()
                                .value("RO60BREL0002002107930103")
                                .type(AccountReferenceType.IBAN)
                                .build())
                        .remittanceInformationUnstructured("13 from: a")
                        .transactionIdGenerated(false)
                        .build())
                .build();
    }
}
