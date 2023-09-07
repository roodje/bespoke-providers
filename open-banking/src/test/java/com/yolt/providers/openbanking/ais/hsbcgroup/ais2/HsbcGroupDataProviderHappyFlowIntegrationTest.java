package com.yolt.providers.openbanking.ais.hsbcgroup.ais2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.hsbcgroup.HsbcGroupApp;
import com.yolt.providers.openbanking.ais.hsbcgroup.HsbcGroupSampleAuthenticationMeansV2;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.HsbcGroupBaseDataProviderV7;
import com.yolt.providers.openbanking.ais.hsbcgroup.common.model.HsbcGroupAccessMeansV2;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.Period;
import java.util.*;
import java.util.stream.Stream;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.openbanking.ais.hsbcgroup.common.auth.HsbcGroupAuthMeansBuilderV3.*;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * This test contains all happy flows occurring in HSBC group group providers.
 * <p>
 * Disclaimer: most providers in HSBC group are the same from code and stubs perspective (then only difference is configuration)
 * The only difference is for balance types in HSBC Corporate provider. Due to that fact this test class is parametrised,
 * so all providers in group are tested.
 * <p>
 * Covered flows:
 * - updating authentication means using autoonboarding
 * - acquiring consent page
 * - fetching accounts, balances, transactions, standing orders, direct debits and parties
 * - creating access means
 * - refreshing access means
 * - deleting consent on bank side
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {HsbcGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("hsbc-generic")
@AutoConfigureWireMock(stubs = {"classpath:/stubs/hsbcgroup/ais-3.1.6/happy-flow"}, httpsPort = 0, port = 0)
class HsbcGroupDataProviderHappyFlowIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = OpenBankingTestObjectMapper.INSTANCE;
    private static final SignerMock SIGNER = new SignerMock();

    private static final UUID USER_ID = UUID.fromString("76640bfe-9a98-441a-8380-c568976eee4a");
    private static final Instant FETCH_FROM = Instant.parse("2020-01-01T00:00:00Z");
    private static final String ACCESS_TOKEN = "3zHagXZSPyA6sifIDmqwZ0hLlqq";
    private static final String ACCESS_TOKEN_CORPO = "rNs2kF3cpk1gD2WjWBmWbrO1vN1";
    private static final String REFRESH_TOKEN = "NfEtxxaLt1SavZW1s7thJ7iw0XZ";

    @Autowired
    @Qualifier("HsbcDataProviderV13")
    private HsbcGroupBaseDataProviderV7 hsbcDataProviderV13;

    @Autowired
    @Qualifier("MarksAndSpencerDataProviderV13")
    private HsbcGroupBaseDataProviderV7 marksAndSpencerDataProviderV13;

    @Autowired
    @Qualifier("FirstDirectDataProviderV13")
    private HsbcGroupBaseDataProviderV7 firstDirectDataProviderV13;

    @Autowired
    @Qualifier("HsbcCorpoDataProviderV11")
    private HsbcGroupBaseDataProviderV7 hsbcCorpoDataProviderV11;


    private Stream<UrlDataProvider> getDataProvidersForPrivateAccounts() {
        return Stream.of(
                marksAndSpencerDataProviderV13,
                hsbcDataProviderV13,
                firstDirectDataProviderV13);
    }

    private Stream<UrlDataProvider> getDataProvidersForBusinessAccounts() {
        return Stream.of(hsbcCorpoDataProviderV11);
    }

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(
                marksAndSpencerDataProviderV13,
                hsbcDataProviderV13,
                firstDirectDataProviderV13,
                hsbcCorpoDataProviderV11
        );
    }

    private String requestTraceId;
    private RestTemplateManager restTemplateManagerMock;
    private Map<String, BasicAuthenticationMean> authenticationMeans;
    private HsbcGroupSampleAuthenticationMeansV2 sampleAuthenticationMeans = new HsbcGroupSampleAuthenticationMeansV2();

    @BeforeEach
    void initialize() throws IOException, URISyntaxException {
        requestTraceId = "d10f24f4-032a-4843-bfc9-22b599c7ae2d";
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
        authenticationMeans = sampleAuthenticationMeans.getHsbcGroupSampleAuthenticationMeansForAis();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnTypedAuthenticationMeansThatWillBeAutoConfigured(AutoOnboardingProvider provider) {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = provider.getAutoConfiguredMeans();

        // then
        assertThat(typedAuthMeans)
                .hasSize(1)
                .containsEntry(CLIENT_ID_NAME, CLIENT_ID_STRING);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnAuthenticationMeansAfterAutoConfiguration(AutoOnboardingProvider provider) throws IOException, URISyntaxException {
        // given
        Map<String, BasicAuthenticationMean> forAutoOnboardingAuthenticationMeans = new HashMap<>(authenticationMeans);
        forAutoOnboardingAuthenticationMeans.remove(CLIENT_ID_NAME);

        UrlAutoOnboardingRequest request = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(forAutoOnboardingAuthenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .setRedirectUrls(Collections.singletonList("https://yolt.com/callback-acc"))
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();

        // when
        Map<String, BasicAuthenticationMean> configuredAuthMeans = provider.autoConfigureMeans(request);

        // then
        assertThat(configuredAuthMeans).hasSize(8);
        assertThat(configuredAuthMeans.get(CLIENT_ID_NAME).getValue()).isEqualTo("some-new-shiny-client-id");
        assertThat(configuredAuthMeans.get(INSTITUTION_ID_NAME).getValue()).isEqualTo("00162010018e22KCTR");
        assertThat(configuredAuthMeans.get(SOFTWARE_STATEMENT_ASSERTION_NAME).getValue()).isEqualTo("fake");
        assertThat(configuredAuthMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()).isEqualTo("5b626fbf-9761-4dfb-a1d6-132f5ee40355");
        assertThat(configuredAuthMeans.get(PRIVATE_SIGNING_KEY_HEADER_ID_NAME).getValue()).isEqualTo("someSigningKeyHeaderId");
        assertThat(configuredAuthMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME).getValue()).isEqualTo("2d4492f7-0188-4cbb-bd0c-c92c034b5cf7");
        assertThat(configuredAuthMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue()).isEqualTo(sampleAuthenticationMeans.readFakeCertificatePem());
        assertThat(configuredAuthMeans.get(SOFTWARE_ID_NAME).getValue()).isEqualTo("someSoftwareId");
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnRedirectStepWithConsentUrl(UrlDataProvider provider) {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .setState("f736527c-c13a-4441-af18-31dd1634e0e3")
                .setBaseClientRedirectUrl("https://yolt.com/callback")
                .build();

        // when
        RedirectStep redirectStep = (RedirectStep) provider.getLoginInfo(request);

        // then
        String loginUrl = redirectStep.getRedirectUrl();
        assertThat(loginUrl).contains("/authorize");

        Map<String, String> queryParams = UriComponentsBuilder.fromUriString(loginUrl).build().getQueryParams().toSingleValueMap();
        assertThat(queryParams)
                .containsEntry("response_type", "code+id_token")
                .containsEntry("client_id", "c54976c8-71a7-4e53-b3a5-b68260698d5e")
                .containsEntry("state", "f736527c-c13a-4441-af18-31dd1634e0e3")
                .containsEntry("scope", "openid+accounts")
                .containsEntry("nonce", "f736527c-c13a-4441-af18-31dd1634e0e3")
                .containsEntry("redirect_uri", "https%3A%2F%2Fyolt.com%2Fcallback")
                .containsKey("request");
        assertThat(redirectStep.getProviderState()).isEqualTo("""
                {"permissions":["ReadParty",\
                "ReadAccountsDetail",\
                "ReadBalances",\
                "ReadDirectDebits",\
                "ReadProducts",\
                "ReadStandingOrdersDetail",\
                "ReadTransactionsCredits",\
                "ReadTransactionsDebits",\
                "ReadTransactionsDetail"]}\
                """);
    }

    @ParameterizedTest
    @MethodSource("getDataProvidersForPrivateAccounts")
    void shouldCorrectlyFetchDataForPrivateAccountsWhenPermissionsListIsInState(UrlDataProvider provider)
            throws TokenInvalidException, ProviderFetchDataException, JsonProcessingException {
        // given
        AccessMeansState<HsbcGroupAccessMeansV2> hsbcGroupAccessMeans = createToken(ACCESS_TOKEN);
        UrlFetchDataRequest urlFetchDataRequest = createUrlFetchDataRequest(createAccessMeansDTO(hsbcGroupAccessMeans));

        // when
        DataProviderResponse dataProviderResponse = provider.fetchData(urlFetchDataRequest);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(3);

        validateCurrentAccount(dataProviderResponse, Optional.of("Full Legal User Name"));
        validateSavingsAccount(dataProviderResponse);
        validateCreditCardAccount(dataProviderResponse);
    }

    @ParameterizedTest
    @MethodSource("getDataProvidersForPrivateAccounts")
    void shouldCorrectlyFetchDataForPrivateAccountsWhenPermissionsListIsNotInState(UrlDataProvider provider)
            throws TokenInvalidException, ProviderFetchDataException, JsonProcessingException {
        // given
        AccessMeansState<HsbcGroupAccessMeansV2> hsbcGroupAccessMeans = createTokenWithoutPermissions(ACCESS_TOKEN);
        UrlFetchDataRequest urlFetchDataRequest = createUrlFetchDataRequest(createAccessMeansDTO(hsbcGroupAccessMeans));

        // when
        DataProviderResponse dataProviderResponse = provider.fetchData(urlFetchDataRequest);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(3);

        validateCurrentAccount(dataProviderResponse, Optional.empty());
        validateSavingsAccount(dataProviderResponse);
        validateCreditCardAccount(dataProviderResponse);
    }

    @ParameterizedTest
    @MethodSource("getDataProvidersForBusinessAccounts")
    void shouldCorrectlyFetchDataForBusinessAccountsWhenPermissionsListIsInState(UrlDataProvider provider)
            throws TokenInvalidException, ProviderFetchDataException, JsonProcessingException {
        // given
        AccessMeansState<HsbcGroupAccessMeansV2> hsbcGroupAccessMeans = createToken(ACCESS_TOKEN_CORPO);
        UrlFetchDataRequest urlFetchDataRequest = createUrlFetchDataRequest(createAccessMeansDTO(hsbcGroupAccessMeans));

        // when
        DataProviderResponse dataProviderResponse = provider.fetchData(urlFetchDataRequest);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(3);

        validateCurrentAccount(dataProviderResponse, Optional.of("User Full Legal Name"));
        validateSavingsAccount(dataProviderResponse);
        validateCreditCardAccount(dataProviderResponse);
    }

    @ParameterizedTest
    @MethodSource("getDataProvidersForBusinessAccounts")
    void shouldCorrectlyFetchDataForBusinessAccountsWhenPermissionsListIsNotInState(UrlDataProvider provider)
            throws TokenInvalidException, ProviderFetchDataException, JsonProcessingException {
        // given
        AccessMeansState<HsbcGroupAccessMeansV2> hsbcGroupAccessMeans = createTokenWithoutPermissions(ACCESS_TOKEN_CORPO);
        UrlFetchDataRequest urlFetchDataRequest = createUrlFetchDataRequest(createAccessMeansDTO(hsbcGroupAccessMeans));

        // when
        DataProviderResponse dataProviderResponse = provider.fetchData(urlFetchDataRequest);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(3);

        validateCurrentAccount(dataProviderResponse, Optional.empty());
        validateSavingsAccount(dataProviderResponse);
        validateCreditCardAccount(dataProviderResponse);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnNewAccessMeans(UrlDataProvider provider) throws JsonProcessingException {
        // given
        UrlCreateAccessMeansRequest request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .setState("29dbba15-1e67-4ac0-ab0f-2487dc0c960b")
                .setProviderState("""
                        {"permissions":["ReadParty",\
                        "ReadAccountsDetail",\
                        "ReadBalances",\
                        "ReadDirectDebits",\
                        "ReadProducts",\
                        "ReadStandingOrdersDetail",\
                        "ReadTransactionsCredits",\
                        "ReadTransactionsDebits",\
                        "ReadTransactionsDetail"]}\
                        """)
                .setRedirectUrlPostedBackFromSite("https://www.yolt.com/callback?state=29dbba15-1e67-4ac0-ab0f-2487dc0c960b&code=iVuATHQHLIjtGjeuxtBj6Gfnd8o")
                .setUserId(USER_ID)
                .build();

        // when
        AccessMeansOrStepDTO accessMeansOrStepDTO = provider.createNewAccessMeans(request);

        // then
        assertThat(accessMeansOrStepDTO.getStep()).isNull();
        AccessMeansDTO accessMeansDTO = accessMeansOrStepDTO.getAccessMeans();
        assertThat(accessMeansDTO.getUserId()).isEqualTo(USER_ID);

        AccessMeansState<HsbcGroupAccessMeansV2> accessMeansState = OpenBankingTestObjectMapper.INSTANCE.readValue(accessMeansDTO.getAccessMeans(), new TypeReference<AccessMeansState<HsbcGroupAccessMeansV2>>() {
        });

        assertThat(accessMeansState.getAccessMeans().getAccessToken()).isEqualTo("3zHagXZSPyA6sifIDmqwZ0hLlqq");
        assertThat(accessMeansState.getAccessMeans().getRefreshToken()).isEqualTo("NfEtxxaLt1SavZW1s7thJ7iw0XZ");
        assertThat(accessMeansState.getAccessMeans().getExpireTime()).isAfter(new Date());
        assertThat(accessMeansState.getAccessMeans().getUpdated()).isCloseTo(new Date(), 5000);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnRefreshedAccessMeansWhenPermissionsListIsInState(UrlDataProvider provider) throws TokenInvalidException, IOException {
        // given
        AccessMeansState<HsbcGroupAccessMeansV2> hsbcGroupAccessMeans = createToken(ACCESS_TOKEN);
        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(createAccessMeansDTO(hsbcGroupAccessMeans))
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();

        // when-then
        AccessMeansDTO accessMeansDTO = provider.refreshAccessMeans(request);

        // then
        assertThat(accessMeansDTO.getUserId()).isEqualTo(USER_ID);

        AccessMeansState<HsbcGroupAccessMeansV2> accessMeansState = OBJECT_MAPPER.readValue(accessMeansDTO.getAccessMeans(), new TypeReference<AccessMeansState<HsbcGroupAccessMeansV2>>() {
        });
        assertThat(accessMeansState.getAccessMeans().getAccessToken()).isEqualTo("2gdG4lTja4FDbPrPRY-UcUpeN5s");
        assertThat(accessMeansState.getAccessMeans().getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(accessMeansState.getPermissions()).containsExactlyInAnyOrderElementsOf(List.of(
                "ReadParty",
                "ReadAccountsDetail",
                "ReadBalances",
                "ReadDirectDebits",
                "ReadProducts",
                "ReadStandingOrdersDetail",
                "ReadTransactionsCredits",
                "ReadTransactionsDebits",
                "ReadTransactionsDetail"
        ));
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnRefreshedAccessMeansWhenPermissionsListIsNotInState(UrlDataProvider provider) throws TokenInvalidException, IOException {
        // given
        AccessMeansState<HsbcGroupAccessMeansV2> hsbcGroupAccessMeans = createTokenWithoutPermissions(ACCESS_TOKEN);
        UrlRefreshAccessMeansRequest request = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(createAccessMeansDTO(hsbcGroupAccessMeans))
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();

        // when-then
        AccessMeansDTO accessMeansDTO = provider.refreshAccessMeans(request);

        // then
        assertThat(accessMeansDTO.getUserId()).isEqualTo(USER_ID);

        AccessMeansState<HsbcGroupAccessMeansV2> accessMeansState = OBJECT_MAPPER.readValue(accessMeansDTO.getAccessMeans(), new TypeReference<AccessMeansState<HsbcGroupAccessMeansV2>>() {
        });
        assertThat(accessMeansState.getAccessMeans().getAccessToken()).isEqualTo("2gdG4lTja4FDbPrPRY-UcUpeN5s");
        assertThat(accessMeansState.getAccessMeans().getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(accessMeansState.getPermissions()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldDeleteUserSite(UrlDataProvider provider) {
        // given
        UrlOnUserSiteDeleteRequest urlGetLogin = new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId("363ca7c1-9d03-4876-8766-ddefc9fd2d76")
                .setSigner(SIGNER)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        ThrowableAssert.ThrowingCallable onUserSiteDeleteCallable = () -> provider.onUserSiteDelete(urlGetLogin);

        // then
        assertThatCode(onUserSiteDeleteCallable).doesNotThrowAnyException();
    }

    private ProviderAccountDTO getAccountById(DataProviderResponse response, String accountId) {
        return response.getAccounts().stream()
                .filter(account -> account.getAccountId().equals(accountId))
                .findFirst()
                .orElseThrow(NullPointerException::new);
    }

    private void validateCurrentAccount(DataProviderResponse dataProviderResponse, Optional<String> holderName) {
        ProviderAccountDTO currentAccount = getAccountById(dataProviderResponse, "28rolohnjgfmdpzzltpie2my76ypcshztfk6");
        assertThat(currentAccount.getAccountId()).isEqualTo("28rolohnjgfmdpzzltpie2my76ypcshztfk6");
        if (holderName.isPresent()) {
            assertThat(currentAccount.getAccountNumber().getHolderName()).isEqualTo(holderName.get());
        } else {
            assertThat(currentAccount.getAccountNumber().getHolderName()).isNull();
        }
        assertThat(currentAccount.getName()).isEqualTo("ROBIN H");
        assertThat(currentAccount.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(currentAccount.getAvailableBalance()).isEqualTo("111.79");
        assertThat(currentAccount.getCurrentBalance()).isEqualTo("110.00");
        assertThat(currentAccount.getClosed()).isNull();
        assertThat(currentAccount.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(currentAccount.getExtendedAccount().getBalances()).hasSize(2);
        assertThat(currentAccount.getStandingOrders()).hasSize(2);
        assertThat(currentAccount.getDirectDebits()).hasSize(2);
        assertThat(currentAccount.getTransactions()).hasSize(3);

        validateCurrentAccountAdditionalData(currentAccount, dataProviderResponse);
        validateCurrentAccountTransactions(currentAccount.getTransactions());
    }

    private void validateSavingsAccount(DataProviderResponse dataProviderResponse) {
        ProviderAccountDTO savingsAccount = getAccountById(dataProviderResponse, "73rl9txy4z65hh3e0gg3rr5laqdbmtuohtvn");
        assertThat(savingsAccount.getAccountId()).isEqualTo("73rl9txy4z65hh3e0gg3rr5laqdbmtuohtvn");
        assertThat(savingsAccount.getName()).isEqualTo("ROBIN H");
        assertThat(savingsAccount.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(savingsAccount.getAvailableBalance()).isEqualTo("5588.55");
        assertThat(savingsAccount.getCurrentBalance()).isEqualTo("1250.00");
        assertThat(savingsAccount.getClosed()).isNull();
        assertThat(savingsAccount.getYoltAccountType()).isEqualTo(AccountType.SAVINGS_ACCOUNT);
        assertThat(savingsAccount.getExtendedAccount().getBalances()).hasSize(2);
        assertThat(savingsAccount.getTransactions()).hasSize(2);
    }

    private void validateCreditCardAccount(DataProviderResponse dataProviderResponse) {
        ProviderAccountDTO creditCardAccount = getAccountById(dataProviderResponse, "XXXX XXXX XXXX 2343");
        assertThat(creditCardAccount.getAccountId()).isEqualTo("XXXX XXXX XXXX 2343");
        assertThat(creditCardAccount.getName()).isEqualTo("HOOD,ROBIN/MR");
        assertThat(creditCardAccount.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(creditCardAccount.getAvailableBalance()).isEqualTo("12000.00");
        assertThat(creditCardAccount.getCurrentBalance()).isEqualTo("0.00");
        assertThat(creditCardAccount.getClosed()).isNull();
        assertThat(creditCardAccount.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
        assertThat(creditCardAccount.getAccountNumber()).isNull();
        assertThat(creditCardAccount.getAccountMaskedIdentification()).isEqualTo("XXXX XXXX XXXX 2343");
        assertThat(creditCardAccount.getExtendedAccount().getBalances()).hasSize(2);
        assertThat(creditCardAccount.getTransactions()).hasSize(3);
    }

    private void validateCurrentAccountTransactions(final List<ProviderTransactionDTO> transactions) {
        // Verify transaction 1
        ProviderTransactionDTO transaction1 = transactions.get(0);
        assertThat(transaction1.getExternalId()).isEqualTo("1232020316166904180020000-0178VB0000000000007000000000000007000000000000)))");
        assertThat(transaction1.getDateTime()).isEqualTo("2020-11-11T00:00Z[Europe/London]");
        assertThat(transaction1.getAmount()).isEqualTo("7.50");
        assertThat(transaction1.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction1.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction1.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction1.getDescription()).isEqualTo("CO-OP GROUP FOOD");

        ExtendedTransactionDTO extendedTransaction = transaction1.getExtendedTransaction();
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("CO-OP GROUP FOOD");
        assertThat(extendedTransaction.getBookingDate()).isEqualTo("2020-11-11T00:00Z[Europe/London]");
        assertThat(extendedTransaction.getValueDate()).isNull();
        assertThat(extendedTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo("-7.50");
        assertThat(extendedTransaction.getTransactionAmount().getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(extendedTransaction.getEntryReference()).isEqualTo("CO-OP GROUP FOOD");

        // Verify transaction 2
        ProviderTransactionDTO transaction2 = transactions.get(1);
        assertThat(transaction2.getExternalId()).isEqualTo("3132020304134101419930000-0220P10000000000010000000000000010000000000000BP");
        assertThat(transaction2.getDateTime()).isEqualTo("2020-10-31T00:00Z[Europe/London]");
        assertThat(transaction2.getAmount()).isEqualTo("10.00");
        assertThat(transaction2.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction2.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction2.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction2.getDescription()).isEqualTo("Batman Monzo");

        // Verify transaction 3
        ProviderTransactionDTO transaction3 = transactions.get(2);
        assertThat(transaction3.getExternalId()).isEqualTo("2312020296026218230150000+0680H 0000000002692340000000002692340000000000CR");
        assertThat(transaction3.getDateTime()).isEqualTo("2020-10-23T01:00+01:00[Europe/London]");
        assertThat(transaction3.getAmount()).isEqualTo("2692.34");
        assertThat(transaction3.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction3.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transaction3.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction3.getDescription()).isEqualTo("GENERAL ACCOUNT");
    }

    private void validateCurrentAccountAdditionalData(ProviderAccountDTO account, DataProviderResponse dataProviderResponse) {
        StandingOrderDTO standingOrderDTO = account.getStandingOrders().get(0);
        assertThat(standingOrderDTO.getDescription()).isEqualTo("MEMBER200");
        assertThat(standingOrderDTO.getFrequency()).isEqualTo(Period.ofDays(1));
        assertThat(standingOrderDTO.getNextPaymentAmount()).isEqualTo(new BigDecimal("29.00"));
        assertThat(standingOrderDTO.getCounterParty().getIdentification()).isEqualTo("20580943876213");

        DirectDebitDTO directDebitDTO = account.getDirectDebits().get(0);
        assertThat(directDebitDTO.getDescription()).isEqualTo("LIMITED");
        assertThat(directDebitDTO.isDirectDebitStatus()).isTrue();
        assertThat(directDebitDTO.getPreviousPaymentAmount()).isEqualTo(new BigDecimal("20.0"));
    }

    private AccessMeansState<HsbcGroupAccessMeansV2> createToken(String accessToken) {
        return new AccessMeansState<>(
                new HsbcGroupAccessMeansV2(
                        Instant.now(),
                        USER_ID,
                        accessToken,
                        REFRESH_TOKEN,
                        Date.from(Instant.now().plus(1, DAYS)),
                        Date.from(Instant.now()),
                        "https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0"),
                List.of("ReadParty",
                        "ReadAccountsDetail",
                        "ReadBalances",
                        "ReadDirectDebits",
                        "ReadProducts",
                        "ReadStandingOrdersDetail",
                        "ReadTransactionsCredits",
                        "ReadTransactionsDebits",
                        "ReadTransactionsDetail"));
    }

    private AccessMeansState<HsbcGroupAccessMeansV2> createTokenWithoutPermissions(String accessToken) {
        return new AccessMeansState<>(
                new HsbcGroupAccessMeansV2(
                        Instant.now(),
                        USER_ID,
                        accessToken,
                        REFRESH_TOKEN,
                        Date.from(Instant.now().plus(1, DAYS)),
                        Date.from(Instant.now()),
                        "https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0"),
                Collections.emptyList());
    }

    private AccessMeansDTO createAccessMeansDTO(AccessMeansState<HsbcGroupAccessMeansV2> oAuthToken) throws JsonProcessingException {
        return new AccessMeansDTO(
                USER_ID,
                OBJECT_MAPPER.writeValueAsString(oAuthToken),
                new Date(),
                Date.from(Instant.now().plus(1, DAYS)));
    }

    private UrlFetchDataRequest createUrlFetchDataRequest(final AccessMeansDTO accessMeansDTO) {
        return new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setTransactionsFetchStartTime(FETCH_FROM)
                .setAccessMeans(accessMeansDTO)
                .setSigner(SIGNER)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .build();
    }


}
