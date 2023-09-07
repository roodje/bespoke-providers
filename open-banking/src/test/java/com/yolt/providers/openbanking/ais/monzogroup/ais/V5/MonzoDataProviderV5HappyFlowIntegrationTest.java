package com.yolt.providers.openbanking.ais.monzogroup.ais.V5;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.monzogroup.MonzoApp;
import com.yolt.providers.openbanking.ais.monzogroup.MonzoSampleTypedAuthMeansV2;
import com.yolt.providers.openbanking.ais.monzogroup.MonzoTestUtilV2;
import com.yolt.providers.openbanking.ais.monzogroup.common.MonzoGroupBaseDataProvider;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains all happy flows occurring in Monzo bank provider.
 * <p>
 * Covered flows:
 * - acquiring consent page
 * - fetching accounts, balances, transactions, standing orders
 * - creating access means
 * - refreshing access means
 * - deleting consent on bank side
 * <p>
 */
@SpringBootTest(classes = {MonzoApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/monzogroup/ob_3.1/ais/happy-flow/", httpsPort = 0, port = 0)
@ActiveProfiles("monzogroup")
public class MonzoDataProviderV5HappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";
    private static AccessMeans VALID_MONZO_ACCESS_MEANS;

    private RestTemplateManagerMock restTemplateManagerMock;

    @Autowired
    @Qualifier("MonzoDataProviderV5")
    private MonzoGroupBaseDataProvider monzoDataProvider;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    List<StubMapping> stubMappings = new ArrayList<>();

    @BeforeAll
    public static void setup() {
        VALID_MONZO_ACCESS_MEANS = new AccessMeans(
                Instant.now(),
                USER_ID,
                "Az90SAOJklae",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL);
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = new MonzoSampleTypedAuthMeansV2().getAuthenticationMeans();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "93bac548-d2de-4546-b106-880a5018460d");
    }

    @AfterEach
    public void afterEach() {
        stubMappings.forEach(WireMock::removeStub);
        stubMappings.clear();
    }

    @Test
    public void shouldReturnConsentPageUrl() throws URISyntaxException {
        // given
        String clientId = "someClientId-2";
        String loginState = UUID.randomUUID().toString();
        String redirectUrl = "http://yolt.com/identifier";

        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl(redirectUrl).setState(loginState)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(new SignerMock())
                .build();

        List<String> expectedQueryParams = Arrays.asList("response_type=code id_token",
                "client_id=" + clientId,
                "state=" + loginState,
                "scope=openid accounts",
                "nonce=" + loginState,
                "redirect_uri=" + redirectUrl);

        // when
        RedirectStep loginInfo = (RedirectStep) monzoDataProvider.getLoginInfo(urlGetLogin);

        // then
        List<String> actualQueryParameters = new URIBuilder(loginInfo.getRedirectUrl())
                .getQueryParams()
                .stream()
                .map(queryParam -> queryParam.getName() + "=" + queryParam.getValue())
                .collect(Collectors.toList());

        assertThat(actualQueryParameters.containsAll(expectedQueryParams)).isEqualTo(true);
        assertThat(loginInfo.getExternalConsentId()).isEqualTo("obaispaccountinformationconsent_0000A48QKMa8mFaTN9Y0LA");
    }

    @Test
    public void shouldRefreshAccessMeans() throws Exception {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, MonzoTestUtilV2.getSerializedAccessMeans(VALID_MONZO_ACCESS_MEANS, objectMapper), new Date(), new Date());
        UrlRefreshAccessMeansRequest urlRefreshAccessMeans = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        AccessMeansDTO retrievedAccessMeans = monzoDataProvider.refreshAccessMeans(urlRefreshAccessMeans);

        // then
        AccessMeans deserializedOAuthToken = objectMapper.readValue(retrievedAccessMeans.getAccessMeans(), AccessMeans.class);
        assertThat(retrievedAccessMeans.getUserId()).isEqualTo(USER_ID);
        assertThat(deserializedOAuthToken.getAccessToken()).isEqualTo("SOME_ACCESS_TOKEN");
        assertThat(deserializedOAuthToken.getRefreshToken()).isEqualTo("SOME_REFRESH_TOKEN");
    }

    @Test
    public void shouldCreateNewAccessMeans() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        String authorizationCode = "sample_auth_code";
        String redirectUrl = "https://www.yolt.com/callback/5fe1e9f8-eb5f-4812-a6a6-2002759db545#code=" + authorizationCode + "&state=secretState";

        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        AccessMeansOrStepDTO newAccessMeans = monzoDataProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThat(newAccessMeans).isNotNull();
    }

    @Test
    public void shouldCorrectlyOnUserSiteDelete() throws TokenInvalidException {
        // given
        UrlOnUserSiteDeleteRequest urlGetLogin = new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId("363ca7c1-9d03-4876-8766-ddefc9fd2d76")
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        monzoDataProvider.onUserSiteDelete(urlGetLogin);
    }

    @Test
    public void shouldReturnCorrectlyFetchData() throws Exception {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, MonzoTestUtilV2.getSerializedAccessMeans(VALID_MONZO_ACCESS_MEANS, objectMapper), new Date(),
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        DataProviderResponse dataProviderResponse = monzoDataProvider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(4);

        ProviderAccountDTO providerAccountDTO = dataProviderResponse.getAccounts().get(0);
        assertThat(providerAccountDTO.getName()).isEqualTo("Bills");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo("147.8500");
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo("147.8500");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);

        // Verify Transactions
        assertThat(providerAccountDTO.getTransactions()).hasSize(3);

        ProviderTransactionDTO firstTransaction = providerAccountDTO.getTransactions().get(0);
        assertThat(firstTransaction.getAmount()).isEqualTo("3.7000");
        assertThat(firstTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(firstTransaction.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(firstTransaction.getDescription()).isEqualTo("Co-op");
        assertThat(firstTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);

        ProviderTransactionDTO secondTransaction = providerAccountDTO.getTransactions().get(1);
        assertThat(secondTransaction.getAmount()).isEqualTo("12.1000");
        assertThat(secondTransaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(secondTransaction.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(secondTransaction.getDescription()).isEqualTo("Sainsbury's");
        assertThat(secondTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);

        // Verify Stand Order
        assertThat(providerAccountDTO.getStandingOrders()).hasSize(1);
        assertThat(providerAccountDTO.getStandingOrders().get(0).getDescription()).isEqualTo("Internet");
        assertThat(providerAccountDTO.getStandingOrders().get(0).getFrequency()).isEqualTo(Period.ofMonths(1));
        assertThat(providerAccountDTO.getStandingOrders().get(0).getNextPaymentAmount()).isEqualTo("0.56");
        assertThat(providerAccountDTO.getStandingOrders().get(0).getCounterParty().getIdentification()).isEqualTo("80200112345678");

        // Verify Direct Debit
        assertThat(providerAccountDTO.getDirectDebits()).hasSize(1);
        assertThat(providerAccountDTO.getDirectDebits().get(0).getDescription()).isEqualTo("JPMC RE PAYPAL INTL LTD");
        assertThat(providerAccountDTO.getDirectDebits().get(0).isDirectDebitStatus()).isTrue();
        assertThat(providerAccountDTO.getDirectDebits().get(0).getPreviousPaymentAmount()).isEqualTo("34.9900");

        // Verify Pots
        ProviderAccountDTO potAccount1 = dataProviderResponse.getAccounts().get(1);
        assertThat(potAccount1.getName()).isEqualTo("Japan 🇯🇵🍙🍜🍱");
        assertThat(potAccount1.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(potAccount1.getCurrentBalance()).isEqualTo("5.0000");
        assertThat(potAccount1.getAvailableBalance()).isEqualTo("5.0000");
        assertThat(potAccount1.getClosed()).isNull();
        assertThat(potAccount1.getYoltAccountType()).isEqualTo(AccountType.SAVINGS_ACCOUNT);

        ProviderAccountDTO potAccount2 = dataProviderResponse.getAccounts().get(2);
        assertThat(potAccount2.getName()).isEqualTo("Bobs And Bits");
        assertThat(potAccount2.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(potAccount2.getCurrentBalance()).isEqualTo("1000.0000");
        assertThat(potAccount2.getAvailableBalance()).isEqualTo("1000.0000");
        assertThat(potAccount2.getClosed()).isNull();
        assertThat(potAccount2.getYoltAccountType()).isEqualTo(AccountType.SAVINGS_ACCOUNT);

        ProviderAccountDTO potAccount3 = dataProviderResponse.getAccounts().get(3);
        assertThat(potAccount3.getName()).isEqualTo("My Savings Pot");
        assertThat(potAccount3.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(potAccount3.getCurrentBalance()).isEqualTo("1100.0000");
        assertThat(potAccount3.getAvailableBalance()).isEqualTo(new BigDecimal("1100.0000"));
        assertThat(potAccount3.getClosed()).isNull();
        assertThat(potAccount3.getYoltAccountType()).isEqualTo(AccountType.SAVINGS_ACCOUNT);
    }
}
