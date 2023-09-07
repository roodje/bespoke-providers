package com.yolt.providers.openbanking.ais.cybgroup.ais.v3;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.common.providerinterface.Provider;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.common.HsmEIdasUtils;
import com.yolt.providers.openbanking.ais.cybgroup.CybgGroupApp;
import com.yolt.providers.openbanking.ais.cybgroup.CybgGroupSampleAuthenticationMeansV2;
import com.yolt.providers.openbanking.ais.cybgroup.common.CybgGroupDataProviderV3;
import com.yolt.providers.openbanking.ais.cybgroup.common.model.CybgGroupAccessMeansV2;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount4Account;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBAccount6;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountSubType1Code;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountType1Code;
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
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.openbanking.ais.cybgroup.common.auth.CybgGroupAuthMeansBuilderV2.*;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * This test contains all happy flows occurring in CYBG group group providers.
 * <p>
 * Disclaimer: all in CYBG group are the same from code and stubs perspective (then only difference is configuration)
 * Due to that fact this test class is parametrised, so all providers in group are tested.
 * <p>
 * Covered flows:
 * - updating authentication means using autoonboarding
 * - acquiring consent page
 * - fetching accounts, balances, transactions, standing orders, direct debits
 * - creating access means
 * - refreshing access means
 * - deleting consent on bank side
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {CybgGroupApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("cybgroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/cybgroup/ais-3.1/happy-flow/", httpsPort = 0, port = 0)
public class CybgGroupDataProviderV3HappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID USER_SITE_ID = UUID.randomUUID();
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REFRESH_TOKEN = "refreshToken";
    private static final String REDIRECT_URL = "http://yolt.com/identifier";

    private String requestTraceId;
    private final RestTemplateManager restTemplateManager = new RestTemplateManagerMock(() -> requestTraceId);

    @Autowired
    @Qualifier("ClydesdaleDataProvider")
    private CybgGroupDataProviderV3 clydesdaleDataProvider;

    @Autowired
    @Qualifier("YorkshireDataProvider")
    private CybgGroupDataProviderV3 yorkshireDataProviderV3;

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(clydesdaleDataProvider, yorkshireDataProviderV3);
    }

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    private static final Signer SIGNER = new SignerMock();

    private final CybgGroupSampleAuthenticationMeansV2 sampleAuthenticationMeans = new CybgGroupSampleAuthenticationMeansV2();
    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        requestTraceId = "4bf28754-9c17-41e6-bc46-6cf98fff679";
        authenticationMeans = sampleAuthenticationMeans.getCybgGroupSampleAuthenticationMeansForAis();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnTypedAuthenticationMeansThatWillBeAutoConfigured(AutoOnboardingProvider provider) {
        // when
        Map<String, TypedAuthenticationMeans> typedAuthMeans = provider.getAutoConfiguredMeans();

        // then
        assertThat(typedAuthMeans)
                .hasSize(3)
                .containsEntry(CLIENT_ID_NAME, CLIENT_ID_STRING)
                .containsEntry(CLIENT_SECRET_NAME, CLIENT_SECRET_STRING)
                .containsEntry(INSTITUTION_ID_NAME, INSTITUTION_ID_STRING);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnAuthenticationMeansAfterAutoConfiguration(AutoOnboardingProvider provider) throws IOException, URISyntaxException {
        // given
        Map<String, BasicAuthenticationMean> forAutoOnboardingAuthenticationMeans = new HashMap<>(authenticationMeans);
        forAutoOnboardingAuthenticationMeans.remove(CLIENT_ID_NAME);

        UrlAutoOnboardingRequest request = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(forAutoOnboardingAuthenticationMeans)
                .setRestTemplateManager(restTemplateManager)
                .setSigner(SIGNER)
                .setRedirectUrls(Collections.singletonList("https://yolt.com/callback-acc"))
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();

        // when
        Map<String, BasicAuthenticationMean> configuredAuthMeans = provider.autoConfigureMeans(request);

        // then
        assertThat(configuredAuthMeans).hasSize(9);
        assertThat(configuredAuthMeans.get(CLIENT_ID_NAME).getValue()).isEqualTo("some-new-shiny-client-id");
        assertThat(configuredAuthMeans.get(CLIENT_SECRET_NAME).getValue()).isEqualTo("some-new-shiny-client-secret");
        assertThat(configuredAuthMeans.get(INSTITUTION_ID_NAME).getValue()).isEqualTo("001728392JD873NNHY");
        assertThat(configuredAuthMeans.get(SOFTWARE_STATEMENT_ASSERTION_NAME).getValue()).isEqualTo("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
        assertThat(configuredAuthMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()).isEqualTo("5b626fbf-9761-4dfb-a1d6-132f5ee40355");
        assertThat(configuredAuthMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue()).isEqualTo("signing-key-header-id");
        assertThat(configuredAuthMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME).getValue()).isEqualTo("11111111-1111-1111-1111-111111111111");
        assertThat(configuredAuthMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue()).isEqualTo(sampleAuthenticationMeans.readFakeCertificatePem());
        assertThat(configuredAuthMeans.get(SOFTWARE_ID_NAME).getValue()).isEqualTo("softwareId");
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnRedirectStepWithConsentUrl(UrlDataProvider provider) {
        // given
        UrlGetLoginRequest request = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManager)
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
                .containsEntry("client_id", "someClientId")
                .containsEntry("state", "f736527c-c13a-4441-af18-31dd1634e0e3")
                .containsEntry("scope", "openid+accounts")
                .containsEntry("nonce", "f736527c-c13a-4441-af18-31dd1634e0e3")
                .containsEntry("redirect_uri", "https%3A%2F%2Fyolt.com%2Fcallback")
                .containsKey("request");
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldCorrectlyFetchData(UrlDataProvider provider) throws JsonProcessingException, TokenInvalidException, ProviderFetchDataException {
        // given
        List<OBAccount6> cachedAccounts = List.of(
                createProviderAccountDTO("92198f42-4726-12fd-ad55-b93b04192121", OBExternalAccountSubType1Code.CURRENTACCOUNT),
                createProviderAccountDTO("b6165a6c-cfbe-6027-8696-163e4557e2f6", OBExternalAccountSubType1Code.SAVINGS),
                createProviderAccountDTO("a134fcf0-3f3a-3dd0-9398-109313b2c6b3", OBExternalAccountSubType1Code.CREDITCARD));

        AccessMeansDTO accessMeansDTO = createAccessMeansDTO(ACCESS_TOKEN, cachedAccounts);
        UrlFetchDataRequest urlFetchData = createUrlFetchDataRequest(accessMeansDTO);

        // when
        DataProviderResponse dataProviderResponse = provider.fetchData(urlFetchData);

        // then
        List<ProviderAccountDTO> accounts = dataProviderResponse.getAccounts();
        assertThat(accounts).hasSize(3);
        dataProviderResponse.getAccounts().forEach(ProviderAccountDTO::validate);

        ProviderAccountDTO currentAccount = accounts.get(0);
        // verify standing orders for current account
        List<StandingOrderDTO> currentAccountStandingOrders = currentAccount.getStandingOrders();
        assertThat(currentAccountStandingOrders).hasSize(1);
        StandingOrderDTO standingOrder1 = currentAccountStandingOrders.get(0);
        assertThat(standingOrder1.getDescription()).isEqualTo("17723262");
        assertThat(standingOrder1.getFrequency()).isEqualTo(Period.ofMonths(1));
        assertThat(standingOrder1.getNextPaymentAmount()).isEqualTo("150.00");
        assertThat(standingOrder1.getCounterParty().getIdentification()).isEqualTo("50506492837451");

        // verify direct debits for current account
        List<DirectDebitDTO> currentAccountDirectDebits = currentAccount.getDirectDebits();
        assertThat(currentAccountDirectDebits).hasSize(2);
        DirectDebitDTO directDebit1 = currentAccountDirectDebits.get(0);
        assertThat(directDebit1.getDescription()).isEqualTo("LIMITED");
        assertThat(directDebit1.isDirectDebitStatus()).isTrue();
        assertThat(directDebit1.getPreviousPaymentAmount()).isEqualTo("43.24");

        ProviderAccountDTO savingAccount = accounts.get(1);
        // verify standing orders for saving account
        List<StandingOrderDTO> savingAccountStandingOrders = savingAccount.getStandingOrders();
        assertThat(savingAccountStandingOrders).isEmpty();

        // verify standing orders for saving account
        List<DirectDebitDTO> savingAccountDirectDebits = savingAccount.getDirectDebits();
        assertThat(savingAccountDirectDebits).isEmpty();

        ProviderAccountDTO creditCardAccount = accounts.get(2);
        // verify standing orders for credit card account
        List<StandingOrderDTO> creditCardAccountStandingOrders = creditCardAccount.getStandingOrders();
        assertThat(creditCardAccountStandingOrders).isEmpty();

        // verify standing orders for credit card account
        List<DirectDebitDTO> creditCardAccountDirectDebits = creditCardAccount.getDirectDebits();
        assertThat(creditCardAccountDirectDebits).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnFetchDataWithSpecificChanges(UrlDataProvider provider) throws JsonProcessingException, TokenInvalidException, ProviderFetchDataException {
        // given
        List<OBAccount6> accounts = Collections.singletonList(createProviderAccountDTO("92198f42-4726-12fd-ad55-b93b04192121", OBExternalAccountSubType1Code.CURRENTACCOUNT));
        AccessMeansDTO accessMeansDTO = createAccessMeansDTO(REFRESH_TOKEN, accounts);

        UrlFetchDataRequest urlFetchData = createUrlFetchDataRequest(accessMeansDTO);

        // when
        DataProviderResponse dataProviderResponse = provider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(1);
        dataProviderResponse.getAccounts().forEach(ProviderAccountDTO::validate);
        ProviderAccountDTO providerAccountDTO = dataProviderResponse.getAccounts().get(0);
        validateTransactions(providerAccountDTO.getTransactions());
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldReturnRefreshedAccessMeans(UrlDataProvider provider) throws TokenInvalidException, IOException {
        // given
        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = createUrlRefreshAccessMeansRequest(REFRESH_TOKEN);

        // when-then
        AccessMeansDTO accessMeansDTO = provider.refreshAccessMeans(urlRefreshAccessMeansRequest);

        // then
        assertThat(accessMeansDTO.getUserId()).isEqualTo(USER_ID);

        CybgGroupAccessMeansV2 refreshedAccessMeans = deserializeToken(accessMeansDTO.getAccessMeans());
        assertThat(accessMeansDTO.getUserId()).isEqualTo(USER_ID);
        assertThat(refreshedAccessMeans.getAccessToken()).isEqualTo("eyJraWQiOiJfNVgyQT92RGE3NWdqRVZDVjM2OFBudnU4NGsiLCJ0eXAiOiJKV1QiLCJhbGciOiJQTzI1NiJ9.eyJhdWQiOiJodHRwczpcL1wveWIuYXBpLW5jLmN5YnNlcnZpY2VzLmNvLnVrIiwic3ViIjoiOGJiZWFkMjUtZmY0MS00OGIwLTk4NDQtZTE0NzEzNGJmODRkIiwib3BlbmJhbmtpbmdfaW50ZW50X2lkIjoiMTNmM2JiYjktOWNiMC00YmQzLWJlMzctY2VlMWM0OWUzYjAxIiwic2NvcGUiOiJhY2NvdW50cyBvcGVuaWQiLCJpc3MiOiJodHRwczpcL1wveWIuYXBpLW5jLmN5YnNlcnZpY2VzLmNvLnVrIiwiZXhwIjoxNjEzNjY3NjE0LCJpYXQiOjE2MTM2NjQwMTQsImNsaWVudF9pZCI6IjhiYmVhZDI1LWZmNDEtNDhiMC05TDQ0LWYxNDcxMzRiZjg0ZCIsImp0aSI6IjI2NmU3Yjc0LWFhZDEtNGFjMC1iNGEzLTc3NjJlMTdlYzViMCJ9.gDT_MWnvXWm9uOlPxi_QntqLPhfpNyf9Wd8KXNjiF80pYicCn1IGx96Y-RauuZGpAyNylKpZESTe_RQXy1ZsCGZ-OJsB5JcArt0BuDAm1UFcCbXwoeQAqyVtMgASAd1B1r4edCJYbA9X_cBUOWQTZcgCfdqWYePrGuYyPlSzKC1zhjav6N4uWgb0ewmn5Zu_Q2RNSS9E-79V5crwzIUVS6YVzZyazhFBzdfpDLweiQtgk9YawYkTE8Sr1RPg8z49GUHn9G30RgaTGQGQ55gxXz9KSmb3RsLvx99XM2Bm6JZCSVUSpTmAmAY_viEfrg_bakyTvlsw-NjzsTOt3EXp-Q");
        assertThat(refreshedAccessMeans.getRefreshToken()).isEqualTo("43ee9a48-b1f9-4f41-8ccc-9f1aa60e71b5");
        assertThat(refreshedAccessMeans.getCachedAccounts()).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldCreateNewAccessMeansAndFetchAccounts(UrlDataProvider provider) throws JsonProcessingException {
        // given
        UUID userId = UUID.randomUUID();
        String authorizationCode = "bd941a87-116c-46b5-915e-47ea05711734";

        final String redirectUrl = "https://www.yolt.com/callback/aff01911-7e22-4b9e-8b86-eae36cf7b732?code=" + authorizationCode + "&state=secretState";

        UrlCreateAccessMeansRequest urlCreateAccessMeans = createUrlCreateAccessMeansRequest(redirectUrl, userId);
        Date twentyNineMinutesFromNow = Date.from(Instant.now().plus(29, ChronoUnit.MINUTES));

        // when
        AccessMeansDTO newAccessMeans = provider.createNewAccessMeans(urlCreateAccessMeans).getAccessMeans();

        // then
        assertThat(twentyNineMinutesFromNow).isBefore((newAccessMeans.getExpireTime()));
        assertThat(newAccessMeans.getUserId()).isEqualTo(userId);

        CybgGroupAccessMeansV2 accessMeans = objectMapper.readValue(newAccessMeans.getAccessMeans(), CybgGroupAccessMeansV2.class);

        List<OBAccount6> accountsList = accessMeans.getCachedAccounts();

        assertThat(accountsList).hasSize(3);

        // Verify account with id = 1 (current account)
        OBAccount6 currentAccount = findProviderAccountDTO(accountsList, "92198f42-4726-12fd-ad55-b93b04192121");

        assertThat(currentAccount.getAccountId()).isEqualTo("92198f42-4726-12fd-ad55-b93b04192121");
        assertThat(currentAccount.getCurrency()).isEqualTo("GBP");
        assertThat(currentAccount.getAccountSubType()).isEqualTo(OBExternalAccountSubType1Code.CURRENTACCOUNT);
        assertThat(currentAccount.getAccountType()).isEqualTo(OBExternalAccountType1Code.PERSONAL);
        List<OBAccount4Account> account1 = currentAccount.getAccount();
        assertThat(account1).hasSize(2);
        OBAccount4Account account1Number1 = account1.get(0);
        assertThat(account1Number1.getSchemeName()).isEqualTo("UK.OBIE.IBAN");
        assertThat(account1Number1.getIdentification()).isEqualTo("GB17ALYD82613220011674");
        assertThat(account1Number1.getName()).isEqualTo("MR ROBIN HOOD");

        OBAccount4Account account1Number2 = account1.get(1);
        assertThat(account1Number2.getSchemeName()).isEqualTo("UK.OBIE.SortCodeAccountNumber");
        assertThat(account1Number2.getIdentification()).isEqualTo("81614220111578");
        assertThat(account1Number2.getName()).isEqualTo("MR ROBIN HOOD");

        // Verify account with id = 2 (savings)
        OBAccount6 savingsAccount = findProviderAccountDTO(accountsList, "b6165a6c-cfbe-6027-8696-163e4557e2f6");

        assertThat(savingsAccount.getAccountId()).isEqualTo("b6165a6c-cfbe-6027-8696-163e4557e2f6");
        assertThat(savingsAccount.getCurrency()).isEqualTo("GBP");
        assertThat(savingsAccount.getAccountSubType()).isEqualTo(OBExternalAccountSubType1Code.SAVINGS);
        assertThat(savingsAccount.getAccountType()).isEqualTo(OBExternalAccountType1Code.PERSONAL);
        List<OBAccount4Account> account2 = savingsAccount.getAccount();
        assertThat(account2).hasSize(2);
        OBAccount4Account account2Number1 = account2.get(0);
        assertThat(account2Number1.getSchemeName()).isEqualTo("UK.OBIE.IBAN");
        assertThat(account2Number1.getIdentification()).isEqualTo("GB76CLYD62613960215826");
        assertThat(account2Number1.getName()).isEqualTo("MR ROBIN HOOD");

        OBAccount4Account account2Number2 = account2.get(1);
        assertThat(account2Number2.getSchemeName()).isEqualTo("UK.OBIE.SortCodeAccountNumber");
        assertThat(account2Number2.getIdentification()).isEqualTo("83613330235823");
        assertThat(account2Number2.getName()).isEqualTo("MR ROBIN HOOD");

        // Verify account with id = 3 (credit card)
        OBAccount6 creditCardAccount = findProviderAccountDTO(accountsList, "a134fcf0-3f3a-3dd0-9398-109313b2c6b3");

        assertThat(creditCardAccount.getAccountId()).isEqualTo("a134fcf0-3f3a-3dd0-9398-109313b2c6b3");
        assertThat(creditCardAccount.getCurrency()).isEqualTo("GBP");
        assertThat(creditCardAccount.getAccountSubType()).isEqualTo(OBExternalAccountSubType1Code.CREDITCARD);
        List<OBAccount4Account> account3 = creditCardAccount.getAccount();
        assertThat(account3).hasSize(1);
        OBAccount4Account account3Number1 = account3.get(0);
        assertThat(account3Number1.getSchemeName()).isEqualTo("UK.OBIE.PAN");
        assertThat(account3Number1.getIdentification()).isEqualTo("************9683");
        assertThat(account3Number1.getName()).isNull();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldDeleteUserSite(UrlDataProvider provider) {
        // given
        UrlOnUserSiteDeleteRequest siteDeleteRequest = createUrlOnUserSiteDeleteRequest("3366f720-26a7-11e8-b65a-bd9397faa378");

        // when
        ThrowableAssert.ThrowingCallable onUserSiteDeleteCallable = () -> provider.onUserSiteDelete(siteDeleteRequest);

        // then
        assertThatCode(onUserSiteDeleteCallable).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnTransportKeyRequirements(Provider provider) {
        // when
        KeyRequirements transportKeyRequirements = provider.getTransportKeyRequirements().get();
        // then
        assertThat(transportKeyRequirements).isEqualTo(HsmEIdasUtils.getKeyRequirements(TRANSPORT_PRIVATE_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnSigningKeyRequirements(Provider provider) {
        // when
        KeyRequirements signingKeyRequirements = provider.getSigningKeyRequirements().get();
        // then
        assertThat(signingKeyRequirements).isEqualTo(HsmEIdasUtils.getKeyRequirements(SIGNING_PRIVATE_KEY_ID_NAME).get());
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnTypedAuthenticationMeans(Provider provider) {
        // when
        Map<String, TypedAuthenticationMeans> authenticationMeans = provider.getTypedAuthenticationMeans();
        // then
        assertThat(authenticationMeans).containsOnlyKeys(
                INSTITUTION_ID_NAME,
                SOFTWARE_ID_NAME,
                CLIENT_ID_NAME,
                CLIENT_SECRET_NAME,
                SIGNING_KEY_HEADER_ID_NAME,
                SIGNING_PRIVATE_KEY_ID_NAME,
                TRANSPORT_CERTIFICATE_NAME,
                TRANSPORT_PRIVATE_KEY_ID_NAME,
                SOFTWARE_STATEMENT_ASSERTION_NAME
        );
    }

    private UrlFetchDataRequest createUrlFetchDataRequest(AccessMeansDTO accessMeans) {
        return new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setUserSiteId(USER_SITE_ID)
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .setRestTemplateManager(restTemplateManager)
                .build();
    }

    private UrlRefreshAccessMeansRequest createUrlRefreshAccessMeansRequest(String refreshToken) throws JsonProcessingException {
        return new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(createAccessMeansDTO(refreshToken, Collections.singletonList(createProviderAccountDTO("92198f42-4726-12fd-ad55-b93b04192121", OBExternalAccountSubType1Code.CURRENTACCOUNT))))
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .setRestTemplateManager(restTemplateManager)
                .build();
    }

    private UrlCreateAccessMeansRequest createUrlCreateAccessMeansRequest(String redirectUrl, UUID userId) {
        return new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .setRestTemplateManager(restTemplateManager)
                .build();
    }

    private UrlOnUserSiteDeleteRequest createUrlOnUserSiteDeleteRequest(String externalConsentId) {
        return new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId(externalConsentId)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .setRestTemplateManager(restTemplateManager)
                .build();
    }

    private OBAccount6 findProviderAccountDTO(final List<OBAccount6> accountDTOs, final String accountId) {
        return accountDTOs.stream()
                .filter(accountDTO -> accountDTO.getAccountId().equalsIgnoreCase(accountId))
                .findFirst()
                .orElse(null);
    }

    private AccessMeansDTO createAccessMeansDTO(String refreshToken, List<OBAccount6> accounts) throws JsonProcessingException {
        AccessMeans accessMeans = new AccessMeans(Instant.now(), USER_ID, ACCESS_TOKEN, refreshToken, getExpirationDate(), new Date(), REDIRECT_URL);
        String providerState = objectMapper.writeValueAsString(new CybgGroupAccessMeansV2(accessMeans, accounts));
        return new AccessMeansDTO(USER_ID, providerState, new Date(), getExpirationDate());
    }

    private Date getExpirationDate() {
        return Date.from(Instant.now().plus(1, DAYS));
    }

    private OBAccount6 createProviderAccountDTO(String accountId, OBExternalAccountSubType1Code accountType) {
        OBAccount6 account = new OBAccount6();
        account.setCurrency("EUR");
        account.setNickname("Test Account");
        account.setAccountSubType(accountType);
        account.setAccountId(accountId);
        account.setAccountType(OBExternalAccountType1Code.PERSONAL);
        OBAccount4Account accountNumber = new OBAccount4Account();
        accountNumber.setSchemeName("UK.OBIE.IBAN");
        accountNumber.setIdentification("IT35 5000 0000 0549 1000 0003");
        account.setAccount(List.of(accountNumber));
        return account;
    }

    private void validateTransactions(List<ProviderTransactionDTO> transactions) {
        assertThat(transactions).hasSize(6);
        ProviderTransactionDTO transaction0 = transactions.get(0);
        assertThat(transaction0.getAmount()).isEqualTo("170.75");
        assertThat(transaction0.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction0.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction0.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction0.getDescription()).isEqualTo("For new bow");

        ProviderTransactionDTO transaction1 = transactions.get(1);
        assertThat(transaction1.getAmount()).isEqualTo("879.00");
        assertThat(transaction1.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction1.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(transaction1.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction1.getDescription()).isEqualTo("More arrows");
    }

    private CybgGroupAccessMeansV2 deserializeToken(final String serializedOAuthToken) throws IOException {
        return objectMapper.readValue(serializedOAuthToken, CybgGroupAccessMeansV2.class);
    }
}
