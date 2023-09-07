package com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais;

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
import com.yolt.providers.monorepogroup.bankvanbredagroup.BankVanBredaGroupDataProvider;
import com.yolt.providers.monorepogroup.bankvanbredagroup.BankVanBredaTestApp;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.BankVanBredaGroupSampleAuthenticationMeans;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.dto.BankVanBredaGroupAccessMeans;
import com.yolt.providers.monorepogroup.bankvanbredagroup.common.ais.auth.dto.TestBankVanBredaGroupTokens;
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
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = BankVanBredaTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/bankvanbredagroup/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("bankvanbreda")
class BankVanBredaGroupDataProviderV1IntegrationTest {

    private static final String TEST_PSU_IP_ADDRESS = "123.45.67.89";
    private static final String CONSENT_ID = "consentId";
    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String REDIRECT_URL = "https://yolt.com/callback-acc";

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("BankVanBredaDataProviderV1")
    private BankVanBredaGroupDataProvider bankVanBredaDataProvider;

    @Autowired
    private Clock clock;

    @Autowired
    @Qualifier("BankVanBredaGroupObjectMapper")
    private ObjectMapper objectMapper;

    public Stream<BankVanBredaGroupDataProvider> getDataProviders() {
        return Stream.of(bankVanBredaDataProvider);
    }

    @BeforeEach
    public void setup() throws IOException, URISyntaxException {
        authenticationMeans = new BankVanBredaGroupSampleAuthenticationMeans().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnRedirectStepOnGetLoginInfo(BankVanBredaGroupDataProvider dataProvider) {
        // given
        String state = UUID.randomUUID().toString();
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(state)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .build();

        // when
        RedirectStep step = dataProvider.getLoginInfo(urlGetLogin);

        // then
        assertThat(step.getExternalConsentId()).isEqualTo("VALID_CONSENT_ID");
        assertThat(step.getProviderState()).isNotNull();
        assertThat(step.getRedirectUrl()).matches(
                "https://xs2a-sandbox-web.bankvanbreda.be/public/berlingroup/authorize/11111111-1111-1111-1111-111111111111\\?" +
                        "scope=AIS:VALID_CONSENT_ID&" +
                        "client_id=PSDNL-SBX-1234512345&" +
                        "state=" + state + "&" +
                        "redirect_uri=https%3A%2F%2Fyolt.com%2Fcallback-acc&" +
                        "code_challenge=[a-zA-Z0-9-_=].*&" +
                        "code_challenge_method=S256&" +
                        "response_type=code");
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldCreateNewAccessMeans(BankVanBredaGroupDataProvider dataProvider) throws JsonProcessingException {
        // given
        String redirectUrlWithCode = REDIRECT_URL + "?code=test-code";
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setUserId(TEST_USER_ID)
                .setRedirectUrlPostedBackFromSite(redirectUrlWithCode)
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setProviderState(CONSENT_ID)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .build();
        String expectedAccessMeans = objectMapper.writeValueAsString(new BankVanBredaGroupAccessMeans(
                new TestBankVanBredaGroupTokens(
                        "new_access_token",
                        1640998800000L,
                        "new_refresh_token"),
                "VALID_CONSENT_ID")
        );

        // when
        AccessMeansOrStepDTO result = dataProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThat(result.getAccessMeans().getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(result.getAccessMeans().getAccessMeans()).isEqualTo(expectedAccessMeans);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldRefreshTokenSuccessfully(BankVanBredaGroupDataProvider dataProvider) throws JsonProcessingException, TokenInvalidException {
        // given
        UrlRefreshAccessMeansRequest refreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(getAccessMeans())
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .build();
        String expectedAccessMeans = objectMapper.writeValueAsString(new BankVanBredaGroupAccessMeans(
                new TestBankVanBredaGroupTokens(
                        "new_access_token",
                        1640998800000L,
                        "refresh_token"),
                "VALID_CONSENT_ID")
        );
        // when
        AccessMeansDTO result = dataProvider.refreshAccessMeans(refreshAccessMeansRequest);

        // then
        assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(result.getAccessMeans()).isEqualTo(expectedAccessMeans);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldDeleteConsentSuccessfully(BankVanBredaGroupDataProvider dataProvider) throws JsonProcessingException {
        // given
        UrlOnUserSiteDeleteRequest onUserSiteDeleteRequest = new UrlOnUserSiteDeleteRequestBuilder()
                .setAccessMeans(getAccessMeans())
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setExternalConsentId(CONSENT_ID)
                .build();
        // when
        ThrowableAssert.ThrowingCallable onUserSiteDeleteCallable = () -> dataProvider.onUserSiteDelete(onUserSiteDeleteRequest);

        // then
        assertThatNoException().isThrownBy(onUserSiteDeleteCallable);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldFetchDataSuccessfully(BankVanBredaGroupDataProvider subject) throws TokenInvalidException, ProviderFetchDataException, JsonProcessingException {
        // given
        UrlFetchDataRequest urlFetchDataRequest = new UrlFetchDataRequestBuilder()
                .setAccessMeans(getAccessMeans())
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setTransactionsFetchStartTime(Instant.now(clock))
                .setUserId(TEST_USER_ID)
                .build();
        DataProviderResponse expectedResponse = createExpectedResponse();

        // when
        DataProviderResponse dataProviderResponse = subject.fetchData(urlFetchDataRequest);

        // then
        assertThat(dataProviderResponse).usingRecursiveComparison()
                .isEqualTo(expectedResponse);
    }

    private AccessMeansDTO getAccessMeans() throws JsonProcessingException {
        BankVanBredaGroupAccessMeans tokens = new BankVanBredaGroupAccessMeans(
                new TestBankVanBredaGroupTokens(
                        "access_token",
                        1003600000L,
                        "refresh_token"),
                "VALID_CONSENT_ID");

        return new AccessMeansDTO(TEST_USER_ID,
                objectMapper.writeValueAsString(tokens),
                new Date(clock.millis()),
                new Date(clock.millis()));
    }

    private DataProviderResponse createExpectedResponse() {
        return new DataProviderResponse(
                List.of(account1(), account2())
        );
    }

    private ZonedDateTime toZonedTime(int year, int month, int day) {
        return ZonedDateTime.of(LocalDate.of(year, month, day), LocalTime.MIN, ZoneId.of(("Europe/Brussels")));
    }

    private ProviderAccountDTO account1() {
        ProviderAccountNumberDTO accountNumber_account1 = new ProviderAccountNumberDTO(IBAN, "FR7630056009271234567890182");
        accountNumber_account1.setHolderName("John Doe");
        return ProviderAccountDTO.builder()
                .accountId("ACCOUNT_ID")
                .name("Main personal account")
                .bic("CCFRFRPP")
                .currency(CurrencyCode.EUR)
                .accountNumber(accountNumber_account1)
                .currentBalance(BigDecimal.valueOf(150.02))
                .availableBalance(BigDecimal.valueOf(150.02))
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .lastRefreshed(ZonedDateTime.now(clock))
                .transactions(List.of(
                        account1Transaction1(),
                        account1Transaction2()))
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("ACCOUNT_ID")
                        .accountReferences(List.of(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("FR7630056009271234567890182")
                                .build()))
                        .currency(CurrencyCode.EUR)
                        .name("Main personal account")
                        .cashAccountType(CURRENT)
                        .bic("CCFRFRPP")
                        .product("SAC")
                        .balances(List.of(
                                BalanceDTO.builder()
                                        .balanceType(BalanceType.INTERIM_AVAILABLE)
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(BigDecimal.valueOf(150.02))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .referenceDate(toZonedTime(2022, 7, 5))
                                        .lastChangeDateTime(toZonedTime(2018, 2, 26))
                                        .build()
                        ))
                        .build())
                .build();
    }

    private ProviderTransactionDTO account1Transaction1() {
        return ProviderTransactionDTO.builder()
                .externalId("1234561")
                .dateTime(toZonedTime(2022, 6, 22))
                .amount(BigDecimal.valueOf(256.61))
                .status(BOOKED)
                .type(DEBIT)
                .description("Example 1")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .entryReference("1234561")
                        .bookingDate(toZonedTime(2022, 6, 21))
                        .valueDate(toZonedTime(2022, 6, 22))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(BigDecimal.valueOf(-256.61))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .creditorName("John Miles")
                        .creditorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("DE43533700240123456901")
                                .build())
                        .remittanceInformationUnstructured("Example 1")
                        .transactionIdGenerated(false)
                        .build())
                .build();
    }

    private ProviderTransactionDTO account1Transaction2() {
        return ProviderTransactionDTO.builder()
                .externalId("1234562")
                .dateTime(toZonedTime(2022, 6, 23))
                .amount(BigDecimal.valueOf(256.62))
                .status(BOOKED)
                .type(DEBIT)
                .description("Example 2")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .entryReference("1234562")
                        .bookingDate(toZonedTime(2022, 6, 22))
                        .valueDate(toZonedTime(2022, 6, 23))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(BigDecimal.valueOf(-256.62))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .creditorName("John Miles")
                        .creditorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("DE43533700240123456902")
                                .build())
                        .remittanceInformationUnstructured("Example 2")
                        .transactionIdGenerated(false)
                        .build())
                .build();
    }

    private ProviderAccountDTO account2() {
        ProviderAccountNumberDTO accountNumber_account1 = new ProviderAccountNumberDTO(IBAN, "FR7611808009101234567890147");
        accountNumber_account1.setHolderName("John Doe");
        return ProviderAccountDTO.builder()
                .accountId("ACCOUNT_ID_2")
                .name("Personal account 2")
                .bic("CMCIFRPAXXX")
                .currency(CurrencyCode.EUR)
                .accountNumber(accountNumber_account1)
                .currentBalance(BigDecimal.valueOf(50.02))
                .availableBalance(BigDecimal.valueOf(50.02))
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .lastRefreshed(ZonedDateTime.now(clock))
                .transactions(List.of(
                        account2Transaction1()))
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("ACCOUNT_ID_2")
                        .accountReferences(List.of(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("FR7611808009101234567890147")
                                .build()))
                        .currency(CurrencyCode.EUR)
                        .name("Personal account 2")
                        .cashAccountType(CURRENT)
                        .bic("CMCIFRPAXXX")
                        .product("SAC")
                        .balances(List.of(
                                BalanceDTO.builder()
                                        .balanceType(BalanceType.INTERIM_AVAILABLE)
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(BigDecimal.valueOf(50.02))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .referenceDate(toZonedTime(2022, 7, 5))
                                        .lastChangeDateTime(toZonedTime(2018, 6, 15))
                                        .build()
                        ))
                        .build())
                .build();
    }

    private ProviderTransactionDTO account2Transaction1() {
        return ProviderTransactionDTO.builder()
                .externalId("1234563")
                .dateTime(toZonedTime(2022, 6, 24))
                .amount(BigDecimal.valueOf(256.63))
                .status(BOOKED)
                .type(DEBIT)
                .description("Example 3")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .entryReference("1234563")
                        .bookingDate(toZonedTime(2022, 6, 23))
                        .valueDate(toZonedTime(2022, 6, 24))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(BigDecimal.valueOf(-256.63))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .creditorName("John Miles")
                        .creditorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("DE43533700240123456903")
                                .build())
                        .remittanceInformationUnstructured("Example 3")
                        .transactionIdGenerated(false)
                        .build())
                .build();
    }
}