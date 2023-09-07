package com.yolt.providers.stet.bnpparibasgroup.ais;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.bnpparibasgroup.BnpParibasGroupDataProvider;
import com.yolt.providers.stet.bnpparibasgroup.BnpParibasGroupTestConfig;
import com.yolt.providers.stet.bnpparibasgroup.bnpparibas.BnpParibasDataProviderV6;
import com.yolt.providers.stet.bnpparibasgroup.common.BnpParibasGroupTestsConstants;
import com.yolt.providers.stet.bnpparibasgroup.common.configuration.BnpParibasGroupSampleAccessMeans;
import com.yolt.providers.stet.bnpparibasgroup.common.configuration.BnpParibasGroupSampleAuthenticationMeans;
import com.yolt.providers.stet.bnpparibasgroup.hellobank.HelloBankDataProviderV6;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.stream.Stream;

import static com.yolt.providers.stet.bnpparibasgroup.common.auth.BnpParibasGroupAuthenticationMeansSupplier.*;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {BnpParibasGroupTestConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("bnpparibasgroup")
@AutoConfigureWireMock(stubs = {"classpath:/stubs/bnpparibasgroup/ais/happy-flow"}, httpsPort = 0, port = 0)
class BnpParibasGroupDataProvidersHappyFlowIntegrationTest {

    private final BnpParibasGroupSampleAccessMeans sampleAccessMeans = new BnpParibasGroupSampleAccessMeans();
    private final BnpParibasGroupSampleAuthenticationMeans sampleAuthenticationMeans = new BnpParibasGroupSampleAuthenticationMeans();
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @Autowired
    private BnpParibasDataProviderV6 bnpParibasDataProvider;

    @Autowired
    private HelloBankDataProviderV6 helloBankDataProvider;

    @Autowired
    @Qualifier("StetObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("HelloBankStetProperties")
    DefaultProperties helloBankProperties;

    @Autowired
    @Qualifier("BnpParibasStetProperties")
    DefaultProperties bnpParibasProperties;

    @Autowired
    private RestTemplateManager restTemplateManagerMock;

    @Autowired
    private Signer signerMock;

    private static final String BASE_CLIENT_REDIRECT_URL = "http://yolt.com/redirect/bnp-paribas";

    public Stream<BnpParibasGroupDataProvider> getProviders() {
        return Stream.of(bnpParibasDataProvider, helloBankDataProvider);
    }

    public Stream<Arguments> getProvidersWithProperties() {
        return Stream.of(
                Arguments.of(bnpParibasDataProvider, bnpParibasProperties),
                Arguments.of(helloBankDataProvider, helloBankProperties)
        );
    }

    @BeforeEach
    void setup() throws IOException, URISyntaxException {

        authenticationMeans = sampleAuthenticationMeans.getBnpSampleAuthenticationMeans();

    }

    @Disabled
    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnAutoConfiguredMeansAfterRegistration(BnpParibasGroupDataProvider dataProvider) {
        // given
        authenticationMeans.remove(CLIENT_ID);
        authenticationMeans.remove(CLIENT_SECRET_NAME);
        authenticationMeans.remove(CLIENT_REGISTRATION_ACCESS_TOKEN_NAME);
        UrlAutoOnboardingRequest autoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signerMock)
                .setBaseClientRedirectUrl("https://www.example.com")
                .build();

        // when
        Map<String, BasicAuthenticationMean> authMeans = dataProvider.autoConfigureMeans(autoOnboardingRequest);

        // then
        assertThat(authMeans.get("client-id").getValue()).isEqualTo("registered-client-id");
        assertThat(authMeans.get("client-secret").getValue()).isEqualTo("registered-client-secret");
        assertThat(authMeans.get("client-registration-access-token").getValue()).isEqualTo("registration-access-token");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldUpdateAutoConfiguredMeansAfterUpdateOfRegistration(BnpParibasGroupDataProvider dataProvider) {
        // given
        UrlAutoOnboardingRequest autoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signerMock)
                .setBaseClientRedirectUrl("https://www.example.com")
                .build();

        // when
        Map<String, BasicAuthenticationMean> authMeans = dataProvider.autoConfigureMeans(autoOnboardingRequest);

        // then
        assertThat(authMeans.get("client-id").getValue()).isEqualTo("client-id");
        assertThat(authMeans.get("client-secret").getValue()).isEqualTo("client-secret");
        assertThat(authMeans.get("client-registration-access-token").getValue()).isEqualTo("87e7c450-a87b-4b2a-a512-37c8c3fc998a");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldReturnProperConsentPageUrl(BnpParibasGroupDataProvider dataProvider) {
        // given
        MultiValueMap<String, String> expectedQueryParams = UriComponentsBuilder.fromHttpUrl("http://someWeb.com/authorize?client_id=client-id&response_type=code&scope=aisp&state=" + BnpParibasGroupTestsConstants.STATE + "&redirect_uri=" + BASE_CLIENT_REDIRECT_URL).build()
                .getQueryParams();
        UrlGetLoginRequest urlGetLoginRequest = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .setState(BnpParibasGroupTestsConstants.STATE)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        RedirectStep response = (RedirectStep) dataProvider.getLoginInfo(urlGetLoginRequest);

        // then
        MultiValueMap<String, String> redirectUrlQueryParameters = UriComponentsBuilder.fromHttpUrl(response.getRedirectUrl()).build().getQueryParams();
        assertThat(redirectUrlQueryParameters).isEqualTo(expectedQueryParams);
    }

    @ParameterizedTest
    @MethodSource("getProvidersWithProperties")
    void shouldCreateNewAccessMeans(BnpParibasGroupDataProvider dataProvider, DefaultProperties properties) {
        // given
        String callbackUrl = String.format("http://yolt.com/redirect/bnp-paribas?code=%s&state=%s", "auth_code", BnpParibasGroupTestsConstants.STATE);
        UrlCreateAccessMeansRequest createAccessMeansRequest = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(BnpParibasGroupTestsConstants.USER_ID)
                .setAuthenticationMeans(authenticationMeans)
                .setRedirectUrlPostedBackFromSite(callbackUrl)
                .setBaseClientRedirectUrl(BASE_CLIENT_REDIRECT_URL)
                .setSigner(signerMock)
                .setRestTemplateManager(restTemplateManagerMock)
                .setProviderState(BnpParibasGroupSampleAuthenticationMeans.createPreAuthorizedJsonProviderState(objectMapper, properties))
                .build();

        // when
        AccessMeansOrStepDTO response = dataProvider.createNewAccessMeans(createAccessMeansRequest);

        // then
        assertThat(response.getAccessMeans().getUserId()).isEqualTo(BnpParibasGroupTestsConstants.USER_ID);
        assertThat(response.getAccessMeans().getAccessMeans()).contains(BnpParibasGroupTestsConstants.ACCESS_TOKEN);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldRefreshAccessMeans(BnpParibasGroupDataProvider dataProvider) throws JsonProcessingException, TokenInvalidException {
        // given
        UrlRefreshAccessMeansRequest accessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(sampleAccessMeans.createAccessMeans(BnpParibasGroupTestsConstants.ACCESS_TOKEN, BnpParibasGroupTestsConstants.USER_ID))
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signerMock)
                .setAccessMeans(sampleAccessMeans.createAccessMeans(BnpParibasGroupTestsConstants.ACCESS_TOKEN, BnpParibasGroupTestsConstants.USER_ID))
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        AccessMeansDTO response = dataProvider.refreshAccessMeans(accessMeansRequest);

        // then
        assertThat(response.getUserId()).isEqualTo(BnpParibasGroupTestsConstants.USER_ID);
        assertThat(response.getAccessMeans()).contains(BnpParibasGroupTestsConstants.ACCESS_TOKEN);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldSuccessfullyFetchData(BnpParibasGroupDataProvider dataProvider) throws ProviderFetchDataException, TokenInvalidException, JsonProcessingException {
        // given
        ZonedDateTime expectedDateTimeOfFirstTransaction = ZonedDateTime.of(2018, 2, 11, 23, 0, 0, 0, ZoneId.of("Z"));
        ZonedDateTime expectedDateTimeOfSecondTransaction = ZonedDateTime.of(2022, 1, 18, 23, 0, 0, 0, ZoneId.of("Z"));
        UrlFetchDataRequest fetchDataRequest = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(BnpParibasGroupTestsConstants.TRANSACTIONS_FETCH_START_TIME)
                .setAccessMeans(sampleAccessMeans.createAccessMeans(BnpParibasGroupTestsConstants.ACCESS_TOKEN, BnpParibasGroupTestsConstants.USER_ID))
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signerMock)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        DataProviderResponse response = dataProvider.fetchData(fetchDataRequest);

        // then
        assertThat(response.getAccounts()).hasSize(2);

        ProviderAccountDTO currentAccount1 = getCurrentAccountById(response, "1");

        /*
            1. Even if TransactionsFetchStartTime is set to date with 534 days ago (which means: 534 / 89 = 6 calls),
               we reduce days back to 365 as the maximum end period indicator.
            2. We do call with filters of 89 days period (or less for last period) with starting since last month (which means: (365-30)/89 = 4,1~ = 5 calls).
                In this test we get 2 transaction per that call.
            3. Summarizing we should have: 5 * 2 = 10 transactions.
         */
        assertThat(currentAccount1.getAccountNumber().getIdentification()).isEqualTo("FR5817569000505918431515C55");
        assertThat(currentAccount1.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.IBAN);
        assertThat(currentAccount1.getTransactions()).hasSize(5 * 3 - 5);
        assertThat(currentAccount1.getTransactions().get(0).getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo("12.25");
        assertThat(currentAccount1.getTransactions().get(0).getAmount()).isEqualTo("12.25");
        assertThat(currentAccount1.getTransactions().get(0).getDateTime()).isEqualTo(expectedDateTimeOfFirstTransaction);
        assertThat(currentAccount1.getTransactions().get(1).getExtendedTransaction().getTransactionAmount().getAmount()).isEqualTo("42.99");
        assertThat(currentAccount1.getTransactions().get(1).getAmount()).isEqualTo("42.99");
        assertThat(currentAccount1.getTransactions().get(1).getDateTime()).isEqualTo(expectedDateTimeOfSecondTransaction);
        assertThat(currentAccount1.getTransactions().get(2).getExtendedTransaction().getValueDate()).isNull();
        assertThat(currentAccount1.getCurrentBalance()).isEqualTo("123.45");
        assertThat(currentAccount1.getExtendedAccount().getBalances()).hasSize(1);

        ExtendedAccountDTO extendedCurrentAccount = currentAccount1.getExtendedAccount();
        assertThat(extendedCurrentAccount.getAccountReferences()).hasSize(1);

        AccountReferenceDTO accountReference = extendedCurrentAccount.getAccountReferences().get(0);
        assertThat(accountReference.getType()).isEqualTo(AccountReferenceType.IBAN);
        assertThat(accountReference.getValue()).isEqualTo("FR5817569000505918431515C55");

        ProviderAccountDTO currentAccount3 = getCurrentAccountById(response, "3");
        assertThat(currentAccount3.getTransactions()).isEmpty();

        response.getAccounts().forEach(ProviderAccountDTO::validate);
    }

    private ProviderAccountDTO getCurrentAccountById(DataProviderResponse response, String accountId) {
        return response.getAccounts().stream()
                .filter(account -> account.getAccountId().equals(accountId))
                .findFirst()
                .orElseThrow(NullPointerException::new);
    }


}
