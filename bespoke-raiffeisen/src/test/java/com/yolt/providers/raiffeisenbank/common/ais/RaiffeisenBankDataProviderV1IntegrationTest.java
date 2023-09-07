package com.yolt.providers.raiffeisenbank.common.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.FormStep;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.ExternalRestTemplateBuilderFactory;
import com.yolt.providers.raiffeisenbank.TestApp;
import com.yolt.providers.raiffeisenbank.common.RaiffeisenBankSampleAuthenticationMeans;
import com.yolt.providers.raiffeisenbank.common.TestRestTemplateManager;
import com.yolt.providers.raiffeisenbank.common.ais.auth.dto.RaiffeisenBankAccessMeans;
import com.yolt.providers.raiffeisenbank.common.ais.auth.dto.RaiffeisenBankTokens;
import com.yolt.providers.raiffeisenbank.common.ais.auth.dto.TestRaiffeisenAuthData;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExchangeRateDTO;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;
import nl.ing.lovebird.providershared.form.TextField;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

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
import static nl.ing.lovebird.extendeddata.account.ExternalCashAccountType.SAVINGS;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.PENDING;
import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.IBAN;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/happy-flow", httpsPort = 0, port = 0)
class RaiffeisenBankDataProviderV1IntegrationTest {

    private static final String TEST_PSU_IP_ADDRESS = "123.45.67.89";
    private static final String REDIRECT_URL = "https://yolt.com/callback-acc ";
    private static final String CONSENT_ID = "7216950142828544";
    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final long FIXED_TIMESTAMP = 1000000000L;

    private TestRestTemplateManager restTemplateManager;
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    @Qualifier("RaiffeisenBankRoDataProviderV1")
    private RaiffeisenBankDataProviderV1 raiffeisenBankRoDataProvider;

    @Autowired
    private ExternalRestTemplateBuilderFactory externalRestTemplateBuilderFactory;

    @MockBean
    private Clock clock;

    @Autowired
    @Qualifier("RaiffeisenBankObjectMapper")
    private ObjectMapper objectMapper;

    public Stream<RaiffeisenBankDataProviderV1> getDataProviders() {
        return Stream.of(raiffeisenBankRoDataProvider);
    }

    @BeforeEach
    public void setup() throws IOException, URISyntaxException {
        when(clock.instant()).thenReturn(Instant.ofEpochMilli(FIXED_TIMESTAMP));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        restTemplateManager = new TestRestTemplateManager(externalRestTemplateBuilderFactory);
        authenticationMeans = new RaiffeisenBankSampleAuthenticationMeans().getAuthenticationMeans();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnFormStepOnGetLoginInfo(RaiffeisenBankDataProviderV1 dataProvider) {
        // given
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl("http://yolt.com/callback-acc")
                .setState(UUID.randomUUID().toString())
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .build();

        // when
        FormStep formStep = dataProvider.getLoginInfo(urlGetLogin);

        // then
        TextField accountLoginTextField = (TextField) formStep.getForm().getFormComponents().get(0);
        assertThat(accountLoginTextField.getId()).isEqualTo("username");
        assertThat(accountLoginTextField.getDisplayName()).isEqualTo("Username");
        assertThat(accountLoginTextField.getLength()).isEqualTo(34);
        assertThat(accountLoginTextField.getMaxLength()).isEqualTo(100);
        TextField ibanTextField = (TextField) formStep.getForm().getFormComponents().get(1);
        assertThat(ibanTextField.getId()).isEqualTo("Iban");
        assertThat(ibanTextField.getDisplayName()).isEqualTo("IBAN");
        assertThat(ibanTextField.getLength()).isEqualTo(34);
        assertThat(ibanTextField.getMaxLength()).isEqualTo(34);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnRedirectStepIfTriggeredAfterFormStep(RaiffeisenBankDataProviderV1 dataProvider) {
        // given
        String stateId = UUID.randomUUID().toString();

        FilledInUserSiteFormValues filledInUserSiteFormValues = new FilledInUserSiteFormValues();
        filledInUserSiteFormValues.add("Iban", "BE16690375703426");
        filledInUserSiteFormValues.add("username", "accountLogin");
        UrlCreateAccessMeansRequest urlCreateAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setFilledInUserSiteFormValues(filledInUserSiteFormValues)
                .setRestTemplateManager(restTemplateManager)
                .setBaseClientRedirectUrl(REDIRECT_URL)
                .setState(stateId)
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .build();

        // when
        RedirectStep redirectStep = (RedirectStep) dataProvider.createNewAccessMeans(urlCreateAccessMeansRequest).getStep();

        // then
        assertThat(redirectStep.getProviderState()).isEqualTo(CONSENT_ID);
        assertThat(redirectStep.getExternalConsentId()).isEqualTo(CONSENT_ID);
        assertThat(redirectStep.getRedirectUrl()).contains("/oauth2/authorize?")
                .contains("client_id=22222222-2222-2222-2222-222222222222")
                .contains("scope=AISP")
                .contains("redirect_uri=https://yolt.com/callback-acc")
                .contains("state=" + stateId)
                .contains("response_type=code")
                .contains("consentId=" + CONSENT_ID);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldCreateNewAccessMeans(RaiffeisenBankDataProviderV1 dataProvider) throws JsonProcessingException {
        // given
        String redirectUrl = "https://yolt.com/callback-acc?code=test-code";
        String baseUrl = "https://yolt.com/callback-acc";
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setUserId(TEST_USER_ID)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setBaseClientRedirectUrl(baseUrl)
                .setProviderState(CONSENT_ID)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .build();
        String expectedAccessMeans = objectMapper.writeValueAsString(new RaiffeisenBankAccessMeans(
                new RaiffeisenBankTokens(
                        new TestRaiffeisenAuthData("new_access_token",
                                100L,
                                "new_refresh_token"),
                        clock),
                CONSENT_ID));

        // when
        AccessMeansOrStepDTO result = dataProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThat(result.getAccessMeans().getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(result.getAccessMeans().getAccessMeans()).isEqualTo(expectedAccessMeans);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldRefreshTokenSuccessfully(RaiffeisenBankDataProviderV1 dataProvider) throws TokenInvalidException, JsonProcessingException {
        // given
        UrlRefreshAccessMeansRequest refreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(getAccessMeans())
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .build();
        String expectedAccessMeans = objectMapper.writeValueAsString(new RaiffeisenBankAccessMeans(
                new RaiffeisenBankTokens(
                        new TestRaiffeisenAuthData("new_refreshed_access_token",
                                100L,
                                "new_refreshed_refresh_token"),
                        clock),
                CONSENT_ID));
        // when
        AccessMeansDTO result = dataProvider.refreshAccessMeans(refreshAccessMeansRequest);

        // then
        assertThat(result.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(result.getAccessMeans()).isEqualTo(expectedAccessMeans);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldDeleteConsentSuccessfully(RaiffeisenBankDataProviderV1 dataProvider) throws JsonProcessingException {
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
    void shouldFetchDataSuccessfully(RaiffeisenBankDataProviderV1 subject) throws TokenInvalidException, ProviderFetchDataException, JsonProcessingException {
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
        assertThat(dataProviderResponse).isEqualTo(expectedResponse);
    }

    private DataProviderResponse createExpectedResponse() {
        return new DataProviderResponse(
                List.of(account1(), account2())
        );
    }

    private ZonedDateTime toZonedTime(int year, int month, int day) {
        return ZonedDateTime.of(LocalDate.of(year, month, day), LocalTime.MIN, ZoneId.of(("Europe/Bucharest")));
    }

    private AccessMeansDTO getAccessMeans() throws JsonProcessingException {
        RaiffeisenBankTokens tokens = new RaiffeisenBankTokens(new TestRaiffeisenAuthData(
                "test-access-token",
                3600L,
                "test-refresh-token"), clock);
        RaiffeisenBankAccessMeans accessMeans = new RaiffeisenBankAccessMeans(tokens, CONSENT_ID);

        return new AccessMeansDTO(TEST_USER_ID,
                objectMapper.writeValueAsString(accessMeans),
                new Date(clock.millis()),
                new Date(clock.millis()));
    }

    private ProviderAccountDTO account1() {
        ProviderAccountNumberDTO accountNumber_account1 = new ProviderAccountNumberDTO(IBAN, "AT611904300234573201");
        accountNumber_account1.setHolderName("John Doe");
        return ProviderAccountDTO.builder()
                .accountId("EX09999991")
                .accountNumber(accountNumber_account1)
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(BigDecimal.valueOf(8623802514669568L))
                .currentBalance(BigDecimal.valueOf(8216935980335104L))
                .currency(CurrencyCode.EUR)
                .name("Main Account1")
                .yoltAccountType(AccountType.CURRENT_ACCOUNT)
                .bic("DABAIE1D")
                .transactions(List.of(
                        account1Transaction1(),
                        account1Transaction2(),
                        account1Transaction3()
                ))
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("EX09999991")
                        .accountReferences(List.of(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("AT611904300234573201")
                                .build()))
                        .currency(CurrencyCode.EUR)
                        .name("Main Account1")
                        .cashAccountType(CURRENT)
                        .bic("DABAIE1D")
                        .product("Main Product1")
                        .balances(List.of(
                                BalanceDTO.builder()
                                        .balanceType(BalanceType.EXPECTED)
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(BigDecimal.valueOf(157505546092544L))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .referenceDate(toZonedTime(2017, 1, 25))
                                        .build(),
                                BalanceDTO.builder()
                                        .balanceType(BalanceType.EXPECTED)
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(BigDecimal.valueOf(8216935980335104L))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .referenceDate(toZonedTime(2017, 1, 26))
                                        .build(),
                                BalanceDTO.builder()
                                        .balanceType(BalanceType.INTERIM_AVAILABLE)
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(BigDecimal.valueOf(8623802514669568L))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .referenceDate(toZonedTime(2017, 1, 25))
                                        .build()
                        ))
                        .build())
                .build();
    }

    private ProviderTransactionDTO account1Transaction3() {
        return ProviderTransactionDTO.builder()
                .externalId("123000002")
                .dateTime(toZonedTime(2018, 1, 2))
                .amount(BigDecimal.valueOf(7458600276459520L))
                .status(BOOKED)
                .type(CREDIT)
                .description("dekkagitcehjuvik")
                .category(YoltCategory.GENERAL)
                .merchant("6011873700900164")
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .entryReference("123000002")
                        .endToEndId("5220032552370176")
                        .mandateId("5/18/2027")
                        .bookingDate(toZonedTime(2018, 1, 1))
                        .valueDate(toZonedTime(2018, 1, 2))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(BigDecimal.valueOf(7458600276459520L))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .exchangeRate(List.of(ExchangeRateDTO.builder()
                                        .currencyFrom(CurrencyCode.EUR)
                                        .currencyTo(CurrencyCode.EUR)
                                        .rateDate(toZonedTime(2006, 1, 13))
                                        .rateFrom("0.241")
                                        .rateContract("5665112388009984")
                                        .build(),
                                ExchangeRateDTO.builder()
                                        .currencyFrom(CurrencyCode.EUR)
                                        .currencyTo(CurrencyCode.EUR)
                                        .rateDate(toZonedTime(2005, 1, 16))
                                        .rateFrom("0.241")
                                        .rateContract("5564361368141824")
                                        .build(),
                                ExchangeRateDTO.builder()
                                        .currencyFrom(CurrencyCode.EUR)
                                        .currencyTo(CurrencyCode.EUR)
                                        .rateDate(toZonedTime(2011, 02, 24))
                                        .rateFrom("0.241")
                                        .rateContract("2477233855791104")
                                        .build()))
                        .creditorName("6011873700900164")
                        .creditorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("AT611904300234573201")
                                .build())
                        .ultimateCreditor("5018374420236570")
                        .debtorName("Alfred Gutierrez")
                        .debtorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("AT611904300234573301")
                                .build())
                        .ultimateDebtor("setfujfopevazbuw")
                        .remittanceInformationUnstructured("dekkagitcehjuvik")
                        .remittanceInformationStructured("asenbufucmecrolw")
                        .purposeCode("oppombakzakupu")
                        .bankTransactionCode("sifhicubiutcikpi")
                        .transactionIdGenerated(false)
                        .build())
                .build();
    }

    private ProviderTransactionDTO account1Transaction2() {
        return ProviderTransactionDTO.builder()
                .externalId("123000010")
                .dateTime(toZonedTime(2018, 1, 2))
                .amount(BigDecimal.valueOf(1475586782396416L))
                .status(PENDING)
                .type(CREDIT)
                .description("gazgu")
                .category(YoltCategory.GENERAL)
                .merchant("6011688244360349")
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(PENDING)
                        .entryReference("123000010")
                        .endToEndId("8693916113043456")
                        .mandateId("6/14/2024")
                        .bookingDate(toZonedTime(2018, 1, 1))
                        .valueDate(toZonedTime(2018, 1, 2))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(BigDecimal.valueOf(1475586782396416L))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .exchangeRate(List.of(ExchangeRateDTO.builder()
                                        .currencyFrom(CurrencyCode.EUR)
                                        .currencyTo(CurrencyCode.EUR)
                                        .rateDate(toZonedTime(2017, 9, 22))
                                        .rateFrom("0.241")
                                        .rateContract("1784568380129280")
                                        .build(),
                                ExchangeRateDTO.builder()
                                        .currencyFrom(CurrencyCode.EUR)
                                        .currencyTo(CurrencyCode.EUR)
                                        .rateDate(toZonedTime(2008, 5, 5))
                                        .rateFrom("0.241")
                                        .rateContract("4985903986507776")
                                        .build(),
                                ExchangeRateDTO.builder()
                                        .currencyFrom(CurrencyCode.EUR)
                                        .currencyTo(CurrencyCode.EUR)
                                        .rateDate(toZonedTime(2014, 3, 04))
                                        .rateFrom("0.241")
                                        .rateContract("1164281766739968")
                                        .build()))
                        .creditorName("6011688244360349")
                        .creditorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("AT611904300234573201")
                                .build())
                        .ultimateCreditor("6334276691503643")
                        .debtorName("Kate Webb")
                        .debtorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("AT611904300234573301")
                                .build())
                        .ultimateDebtor("mopmuslulirzucuv")
                        .remittanceInformationUnstructured("gazgu")
                        .remittanceInformationStructured("dawfa")
                        .purposeCode("oror")
                        .bankTransactionCode("avatmasnoveigjaf")
                        .transactionIdGenerated(false)
                        .build())
                .build();
    }

    private ProviderTransactionDTO account1Transaction1() {
        return ProviderTransactionDTO.builder()
                .externalId("123000001")
                .dateTime(toZonedTime(2018, 1, 2))
                .amount(BigDecimal.valueOf(3789661851877376L))
                .status(BOOKED)
                .type(CREDIT)
                .description("nanrunalasowubse")
                .category(YoltCategory.GENERAL)
                .merchant("36848963124227")
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .entryReference("123000001")
                        .endToEndId("1109085025992704")
                        .mandateId("8/21/2108")
                        .bookingDate(toZonedTime(2018, 1, 1))
                        .valueDate(toZonedTime(2018, 1, 2))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(BigDecimal.valueOf(3789661851877376L))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .exchangeRate(List.of(ExchangeRateDTO.builder()
                                        .currencyFrom(CurrencyCode.EUR)
                                        .currencyTo(CurrencyCode.EUR)
                                        .rateDate(toZonedTime(2002, 10, 11))
                                        .rateFrom("0.241")
                                        .rateContract("4082602069721088")
                                        .build(),
                                ExchangeRateDTO.builder()
                                        .currencyFrom(CurrencyCode.EUR)
                                        .currencyTo(CurrencyCode.EUR)
                                        .rateDate(toZonedTime(2017, 11, 27))
                                        .rateFrom("0.241")
                                        .rateContract("1328627138428928")
                                        .build(),
                                ExchangeRateDTO.builder()
                                        .currencyFrom(CurrencyCode.EUR)
                                        .currencyTo(CurrencyCode.EUR)
                                        .rateDate(toZonedTime(2013, 12, 12))
                                        .rateFrom("0.241")
                                        .rateContract("7555606961455104")
                                        .build()))
                        .creditorName("36848963124227")
                        .creditorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("AT611904300234573201")
                                .build())
                        .ultimateCreditor("5488596990242908")
                        .debtorName("Mike Fleming")
                        .debtorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("AT611904300234573301")
                                .build())
                        .ultimateDebtor("pepgiwaaptudifri")
                        .remittanceInformationUnstructured("nanrunalasowubse")
                        .remittanceInformationStructured("lemohijik")
                        .purposeCode("nemisufiemui")
                        .bankTransactionCode("zisamogumbo")
                        .transactionIdGenerated(false)
                        .build())
                .build();
    }

    private ProviderAccountDTO account2() {
        ProviderAccountNumberDTO accountNumber_account1 = new ProviderAccountNumberDTO(IBAN, "AT611904300234573202");
        accountNumber_account1.setHolderName("John Doe");
        return ProviderAccountDTO.builder()
                .accountId("EX09999992")
                .accountNumber(accountNumber_account1)
                .lastRefreshed(ZonedDateTime.now(clock))
                .availableBalance(BigDecimal.valueOf(7751577808928768L))
                .currentBalance(BigDecimal.valueOf(5181686765584384L))
                .currency(CurrencyCode.EUR)
                .name("Main Account2")
                .yoltAccountType(AccountType.SAVINGS_ACCOUNT)
                .bic("DABAIE2D")
                .transactions(List.of(account2Transaction1()))
                .extendedAccount(ExtendedAccountDTO.builder()
                        .resourceId("EX09999992")
                        .accountReferences(List.of(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("AT611904300234573202")
                                .build()))
                        .currency(CurrencyCode.EUR)
                        .name("Main Account2")
                        .cashAccountType(SAVINGS)
                        .bic("DABAIE2D")
                        .product("Main Product2")
                        .balances(List.of(
                                BalanceDTO.builder()
                                        .balanceType(BalanceType.EXPECTED)
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(BigDecimal.valueOf(5181686765584384L))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .referenceDate(toZonedTime(2017, 2, 25))
                                        .build(),
                                BalanceDTO.builder()
                                        .balanceType(BalanceType.INTERIM_AVAILABLE)
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(BigDecimal.valueOf(7751577808928768L))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .referenceDate(toZonedTime(2017, 2, 25))
                                        .build(),
                                BalanceDTO.builder()
                                        .balanceType(BalanceType.INTERIM_AVAILABLE)
                                        .balanceAmount(BalanceAmountDTO.builder()
                                                .amount(BigDecimal.valueOf(6953610925572096L))
                                                .currency(CurrencyCode.EUR)
                                                .build())
                                        .referenceDate(toZonedTime(2017, 2, 25))
                                        .build()
                        ))
                        .build())
                .build();
    }

    private ProviderTransactionDTO account2Transaction1() {
        return ProviderTransactionDTO.builder()
                .externalId("123000003")
                .dateTime(toZonedTime(2018, 1, 2))
                .amount(BigDecimal.valueOf(22196791541760L))
                .status(BOOKED)
                .type(CREDIT)
                .description("opevapulucalel")
                .category(YoltCategory.GENERAL)
                .merchant("5610754844080575")
                .extendedTransaction(ExtendedTransactionDTO.builder()
                        .status(BOOKED)
                        .entryReference("123000003")
                        .endToEndId("6032334822309888")
                        .mandateId("7/11/2022")
                        .bookingDate(toZonedTime(2018, 1, 1))
                        .valueDate(toZonedTime(2018, 1, 2))
                        .transactionAmount(BalanceAmountDTO.builder()
                                .amount(BigDecimal.valueOf(22196791541760L))
                                .currency(CurrencyCode.EUR)
                                .build())
                        .exchangeRate(List.of(ExchangeRateDTO.builder()
                                        .currencyFrom(CurrencyCode.EUR)
                                        .currencyTo(CurrencyCode.EUR)
                                        .rateDate(toZonedTime(2014, 12, 11))
                                        .rateFrom("0.241")
                                        .rateContract("1540398856011776")
                                        .build(),
                                ExchangeRateDTO.builder()
                                        .currencyFrom(CurrencyCode.EUR)
                                        .currencyTo(CurrencyCode.EUR)
                                        .rateDate(toZonedTime(2015, 9, 17))
                                        .rateFrom("0.241")
                                        .rateContract("8190866466275328")
                                        .build(),
                                ExchangeRateDTO.builder()
                                        .currencyFrom(CurrencyCode.EUR)
                                        .currencyTo(CurrencyCode.EUR)
                                        .rateDate(toZonedTime(2015, 10, 15))
                                        .rateFrom("0.241")
                                        .rateContract("5773558059892736")
                                        .build()))
                        .creditorName("5610754844080575")
                        .creditorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("AT611904300234573201")
                                .build())
                        .ultimateCreditor("5146114389566053")
                        .debtorName("Mike Hall")
                        .debtorAccount(AccountReferenceDTO.builder()
                                .type(AccountReferenceType.IBAN)
                                .value("AT611904300234573301")
                                .build())
                        .ultimateDebtor("zeobamocza")
                        .remittanceInformationUnstructured("opevapulucalel")
                        .remittanceInformationStructured("mehefze")
                        .purposeCode("italuluursus")
                        .bankTransactionCode("mubem")
                        .transactionIdGenerated(false)
                        .build())
                .build();
    }
}