package com.yolt.providers.stet.labanquepostale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.GenericDataProvider;
import com.yolt.providers.stet.generic.GenericOnboardingDataProvider;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.labanquepostalegroup.labanquepostale.LaBanquePostaleDataProviderV5;
import com.yolt.providers.stet.labanquepostalegroup.labanquepostale.config.LaBanquePostaleProperties;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.*;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import static com.yolt.providers.stet.labanquepostalegroup.labanquepostale.auth.LaBanquePostaleAuthenticationMeansSupplier.CLIENT_ID_NAME;
import static com.yolt.providers.stet.labanquepostalegroup.labanquepostale.auth.LaBanquePostaleAuthenticationMeansSupplier.CLIENT_SECRET_NAME;
import static nl.ing.lovebird.extendeddata.common.CurrencyCode.EUR;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;
import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.PENDING;
import static nl.ing.lovebird.providerdomain.AccountType.CREDIT_CARD;
import static nl.ing.lovebird.providerdomain.AccountType.CURRENT_ACCOUNT;
import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.IBAN;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.CREDIT;
import static nl.ing.lovebird.providerdomain.ProviderTransactionType.DEBIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = LaBanquePostaleGroupTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("labanquepostale")
@AutoConfigureWireMock(stubs = {
        "classpath:/stubs/labanquepostale/ais/happy-flow",
        "classpath:/stubs/labanquepostale/registration"}, httpsPort = 0, port = 0)
class LaBanquePostaleGroupDataProviderHappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String CLIENT_ID = "da5759d1-8f95-4b31-8183-fab1c62b52c5";
    private static final String CLIENT_SECRET = "9bd412c1-58da-4be6-82dc-9809f43eeae9";
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String BASE_CLIENT_REDIRECT_URL = "https://yolt.com/";
    private static final String ENCODED_BASE_CLIENT_REDIRECT_URL = "https%3A%2F%2Fyolt.com%2F";
    private static final String REDIRECT_URL_POSTED_BACK_FROM_SITE = "https://yolt.com/?code=5821";
    private static final String ACCESS_TOKEN = "af39f12f-eb22-4226-aa4e-5f68ba840c55";
    private static final String REFRESH_TOKEN = "d0710783-fb31-41af-9288-8d5b8da755e5";

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private Signer signer;

    @Autowired
    @Qualifier("LaBanquePostaleStetProperties")
    private LaBanquePostaleProperties laBanquePostaleProperties;

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("LaBanquePostaleDataProviderV5")
    private LaBanquePostaleDataProviderV5 laBanquePostaleDataProvider;

    private Stream<Arguments> getDataProviders() {
        return Stream.of(Arguments.of(laBanquePostaleDataProvider, laBanquePostaleProperties));
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnClientSpecificDetailsAfterAutoOnboarding(GenericOnboardingDataProvider dataProvider) {
        // given
        UrlAutoOnboardingRequest request = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(LaBanquePostaleGroupSampleMeans.getPreconfiguredBasicAuthenticationMeans())
                .setRestTemplateManager(restTemplateManager)
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .build();

        // when
        Map<String, BasicAuthenticationMean> basicAuthMeans = dataProvider.autoConfigureMeans(request);

        // then
        assertThat(basicAuthMeans.get(CLIENT_ID_NAME).getValue()).isEqualTo(CLIENT_ID);
        assertThat(basicAuthMeans.get(CLIENT_SECRET_NAME).getValue()).isEqualTo(CLIENT_SECRET);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnConsentPageUrl(GenericDataProvider dataProvider) {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(LaBanquePostaleGroupSampleMeans.getConfiguredAuthenticationMeans())
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .setState("state")
                .build();

        // when
        RedirectStep step = (RedirectStep) dataProvider.getLoginInfo(request);

        // then
        assertThat(step.getRedirectUrl()).containsSequence("/authorize?" + new StringJoiner("&")
                .add("client_id=" + CLIENT_ID)
                .add("response_type=" + OAuth.CODE)
                .add("scope=" + Scope.AISP.getValue())
                .add("state=state")
                .add("redirect_uri=" + ENCODED_BASE_CLIENT_REDIRECT_URL));
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldCreateNewAccessMeans(GenericDataProvider dataProvider, DefaultProperties properties) {
        // given
        String jsonProviderState = LaBanquePostaleGroupSampleMeans.createPreAuthorizedJsonProviderState(objectMapper, properties);

        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(LaBanquePostaleGroupSampleMeans.getConfiguredAuthenticationMeans())
                .setUserId(USER_ID)
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .setRedirectUrlPostedBackFromSite(REDIRECT_URL_POSTED_BACK_FROM_SITE)
                .setRestTemplateManager(restTemplateManager)
                .setProviderState(jsonProviderState)
                .build();

        // when
        AccessMeansOrStepDTO accessMeansOrStepDTO = dataProvider.createNewAccessMeans(request);

        // then
        assertThat(accessMeansOrStepDTO.getStep()).isNull();

        AccessMeansDTO accessMeansDTO = accessMeansOrStepDTO.getAccessMeans();
        assertThat(accessMeansDTO.getUserId()).isEqualTo(USER_ID);
        assertThat(accessMeansDTO.getExpireTime()).isAfter(new Date());
        assertThat(accessMeansDTO.getUpdated()).isCloseTo(new Date(), Duration.ofSeconds(3).toMillis());

        DataProviderState providerState = LaBanquePostaleGroupSampleMeans.deserializeJsonProviderState(objectMapper, accessMeansDTO.getAccessMeans());
        assertThat(providerState.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(providerState.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(providerState.getRegion()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldThrowUnsupportedOperationExceptionDueToUnsupportedRefreshFlow(GenericDataProvider dataProvider, DefaultProperties properties) {
        // given
        String jsonProviderState = LaBanquePostaleGroupSampleMeans.createAuthorizedJsonProviderState(objectMapper, properties, ACCESS_TOKEN, REFRESH_TOKEN);

        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setSigner(signer)
                .setAccessMeans(USER_ID, jsonProviderState, new Date(), new Date())
                .setAuthenticationMeans(LaBanquePostaleGroupSampleMeans.getConfiguredAuthenticationMeans())
                .setRestTemplateManager(restTemplateManager)
                .build();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> dataProvider.refreshAccessMeans(request);

        // then
        assertThatThrownBy(throwingCallable)
                .isInstanceOf(TokenInvalidException.class);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldSuccessfullyFetchData(GenericDataProvider dataProvider, DefaultProperties properties) throws TokenInvalidException, ProviderFetchDataException {
        // given
        String jsonProviderState = LaBanquePostaleGroupSampleMeans.createAuthorizedJsonProviderState(objectMapper, properties, ACCESS_TOKEN);

        UrlFetchDataRequest request = new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setUserSiteId(UUID.randomUUID())
                .setAccessMeans(USER_ID, jsonProviderState, new Date(), new Date())
                .setAuthenticationMeans(LaBanquePostaleGroupSampleMeans.getConfiguredAuthenticationMeans())
                .setPsuIpAddress(PSU_IP_ADDRESS)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(signer)
                .setTransactionsFetchStartTime(Instant.now())
                .build();

        DataProviderResponse expectedResult = new DataProviderResponse(getAccounts());

        // when
        DataProviderResponse response = dataProvider.fetchData(request);

        //then
        List<ProviderAccountDTO> accounts = response.getAccounts();
        List<ProviderAccountDTO> expectedAccounts = expectedResult.getAccounts();
        assertThat(accounts).hasSize(expectedAccounts.size());
        for (int i = 0; i < expectedAccounts.size(); i++) {
            ProviderAccountDTO account = accounts.get(i);
            ProviderAccountDTO expectedAccount = expectedAccounts.get(i);
            assertThatObjects(account, expectedAccount).areEqualIgnoringFields("lastRefreshed", "transactions", "extendedAccount");
            assertThatObjects(account.getExtendedAccount(), expectedAccount.getExtendedAccount()).areEqual();
            assertThat(account.getLastRefreshed()).isNotNull();

            //Verify Transactions
            List<ProviderTransactionDTO> transactions = account.getTransactions();
            List<ProviderTransactionDTO> expectedTransactions = expectedAccount.getTransactions();
            assertThat(transactions).hasSize(expectedTransactions.size());
            for (int j = 0; j < expectedTransactions.size(); j++) {
                ProviderTransactionDTO transaction = transactions.get(j);
                ProviderTransactionDTO expectedTransaction = expectedTransactions.get(j);
                assertThatObjects(transaction, expectedTransaction).areEqualIgnoringFields("extendedTransaction");
                assertThatObjects(transaction.getExtendedTransaction(), expectedTransaction.getExtendedTransaction()).areEqual();
            }
        }
    }

    private List<ProviderAccountDTO> getAccounts() {
        ProviderAccountNumberDTO accountNumber1 = new ProviderAccountNumberDTO(IBAN, "FR8414508000709133733381U45");
        accountNumber1.setHolderName("NAME OF THE ACCOUNT");
        ProviderAccountNumberDTO accountNumber2 = new ProviderAccountNumberDTO(IBAN, "FR9317569000502693659367F19");
        accountNumber2.setHolderName("Current-Account-2 with OTHR balance and empty transactions");
        ProviderAccountNumberDTO accountNumber3 = new ProviderAccountNumberDTO(IBAN, "FR3930003000505784737239O11");
        accountNumber3.setHolderName("Card-Account-3 Credit Card Account");
        return Arrays.asList(ProviderAccountDTO.builder()
                        .yoltAccountType(CURRENT_ACCOUNT)
                        .availableBalance(new BigDecimal("1.00"))
                        .currentBalance(new BigDecimal("1.00"))
                        .accountId("1")
                        .accountNumber(accountNumber1)
                        .name("NAME OF THE ACCOUNT")
                        .currency(CurrencyCode.EUR)
                        .transactions(getProviderTransactions1())
                        .extendedAccount(getExtendedAccount1())
                        .build(),
                ProviderAccountDTO.builder()
                        .yoltAccountType(CURRENT_ACCOUNT)
                        .availableBalance(new BigDecimal("84.11"))
                        .currentBalance(new BigDecimal("84.11"))
                        .accountId("2")
                        .accountNumber(accountNumber2)
                        .name("Current-Account-2 with OTHR balance and empty transactions")
                        .currency(CurrencyCode.EUR)
                        .transactions(getProviderTransactions2())
                        .extendedAccount(getExtendedAccount2())
                        .build(),
                ProviderAccountDTO.builder()
                        .yoltAccountType(CREDIT_CARD)
                        .availableBalance(new BigDecimal("15.09"))
                        .currentBalance(new BigDecimal("15.09"))
                        .accountId("3")
                        .accountNumber(accountNumber3)
                        .name("Card-Account-3 Credit Card Account")
                        .currency(CurrencyCode.EUR)
                        .creditCardData(ProviderCreditCardDTO.builder()
                                .availableCreditAmount(new BigDecimal("15.09"))
                                .build())
                        .transactions(getProviderTransactions3())
                        .extendedAccount(getExtendedAccount3())
                        .build());
    }

    private ExtendedAccountDTO getExtendedAccount1() {
        return ExtendedAccountDTO.builder()
                .resourceId("1")
                .accountReferences(Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, "FR8414508000709133733381U45")))
                .currency(EUR)
                .name("NAME OF THE ACCOUNT")
                .balances(getBalances1())
                .cashAccountType(ExternalCashAccountType.CURRENT)
                .usage(UsageType.PRIVATE)
                .status(Status.ENABLED)
                .build();
    }

    private List<ProviderTransactionDTO> getProviderTransactions1() {
        return Arrays.asList(ProviderTransactionDTO.builder()
                        .dateTime(getDateTime("2020-09-06T00:00+02:00[Europe/Paris]"))
                        .amount(new BigDecimal("51.00"))
                        .status(BOOKED)
                        .type(DEBIT)
                        .description("SABUKI DENERIO PORRINES, MR DERROS MARINES, COMPTE FR111111111111111111T, REFERENCE : 1111111111111")
                        .category(YoltCategory.GENERAL)
                        .extendedTransaction(getExtendedTransaction1_1())
                        .build(),
                ProviderTransactionDTO.builder()
                        .dateTime(getDateTime("2018-02-12T00:00+01:00[Europe/Paris]"))
                        .amount(new BigDecimal("12.25"))
                        .status(BOOKED)
                        .type(CREDIT)
                        .description("SEPA CREDIT TRANSFER from PSD2Company")
                        .category(YoltCategory.GENERAL)
                        .extendedTransaction(getExtendedTransaction1_2())
                        .build());
    }

    private ExtendedTransactionDTO getExtendedTransaction1_1() {
        return ExtendedTransactionDTO.builder()
                .status(BOOKED)
                .bookingDate(getDateTime("2020-09-06T00:00+02:00[Europe/Paris]"))
                .transactionAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("-51.00")))
                .remittanceInformationUnstructured("SABUKI DENERIO PORRINES, MR DERROS MARINES, COMPTE FR111111111111111111T, REFERENCE : 1111111111111")
                .build();
    }

    private ExtendedTransactionDTO getExtendedTransaction1_2() {
        return ExtendedTransactionDTO.builder()
                .status(BOOKED)
                .bookingDate(getDateTime("2018-02-12T00:00+01:00[Europe/Paris]"))
                .transactionAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("12.25")))
                .remittanceInformationUnstructured("SEPA CREDIT TRANSFER from PSD2Company")
                .build();
    }

    private List<BalanceDTO> getBalances1() {
        return Collections.singletonList(BalanceDTO.builder()
                .balanceType(BalanceType.EXPECTED)
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("1.00")))
                .build()
        );
    }

    private ExtendedAccountDTO getExtendedAccount2() {
        return ExtendedAccountDTO.builder()
                .resourceId("2")
                .accountReferences(Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, "FR9317569000502693659367F19")))
                .currency(EUR)
                .name("Current-Account-2 with OTHR balance and empty transactions")
                .cashAccountType(ExternalCashAccountType.CURRENT)
                .balances(getBalances2())
                .status(Status.ENABLED)
                .build();
    }

    private List<ProviderTransactionDTO> getProviderTransactions2() {
        return Collections.emptyList();
    }

    private List<BalanceDTO> getBalances2() {
        return Collections.singletonList(
                BalanceDTO.builder()
                        .balanceAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("84.11")))
                        .balanceType(BalanceType.AUTHORISED)
                        .build()
        );
    }

    private ExtendedAccountDTO getExtendedAccount3() {
        return ExtendedAccountDTO.builder()
                .resourceId("3")
                .accountReferences(Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, "FR3930003000505784737239O11")))
                .currency(EUR)
                .name("Card-Account-3 Credit Card Account")
                .cashAccountType(ExternalCashAccountType.CURRENT)
                .balances(getBalances3())
                .status(Status.ENABLED)
                .build();
    }

    private List<ProviderTransactionDTO> getProviderTransactions3() {
        return Collections.singletonList(ProviderTransactionDTO.builder()
                .dateTime(getDateTime("2020-04-30T00:00+02:00[Europe/Paris]"))
                .amount(new BigDecimal("8.42"))
                .status(PENDING)
                .type(DEBIT)
                .description("GEANT BB840, null, null, null")
                .category(YoltCategory.GENERAL)
                .extendedTransaction(getExtendedTransaction3())
                .build());
    }

    private ExtendedTransactionDTO getExtendedTransaction3() {
        return ExtendedTransactionDTO.builder()
                .status(PENDING)
                .bookingDate(getDateTime("2020-04-30T00:00+02:00[Europe/Paris]"))
                .transactionAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("-8.42")))
                .remittanceInformationUnstructured("GEANT BB840, null, null, null")
                .build();
    }

    private List<BalanceDTO> getBalances3() {
        return Collections.singletonList(BalanceDTO.builder()
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.EUR, new BigDecimal("15.09")))
                .balanceType(BalanceType.AUTHORISED)
                .build()
        );
    }

    private ZonedDateTime getDateTime(final String dateTime) {
        return ZonedDateTime.parse(dateTime);
    }

    private ComparedObjects assertThatObjects(Object obj1, Object obj2) {
        return new ComparedObjects(obj1, obj2);
    }

    @RequiredArgsConstructor
    private static class ComparedObjects {

        private final Object obj1;
        private final Object obj2;

        void areEqualIgnoringFields(String... ignoringFields) {
            assertThat(obj1)
                    .usingRecursiveComparison()
                    .ignoringFields(ignoringFields)
                    .isEqualTo(obj2);
        }

        void areEqual() {
            areEqualIgnoringFields();
        }
    }

}
