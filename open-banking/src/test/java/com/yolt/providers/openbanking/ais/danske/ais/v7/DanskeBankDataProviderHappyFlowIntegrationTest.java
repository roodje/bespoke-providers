package com.yolt.providers.openbanking.ais.danske.ais.v7;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.autoonboarding.RegistrationOperation;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.openbanking.ais.TestConfiguration;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.danske.DanskeApp;
import com.yolt.providers.openbanking.ais.danske.DanskeBankDataProviderV7;
import com.yolt.providers.openbanking.ais.danske.DanskeBankSampleTypedAuthenticationMeansV7;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.yolt.providers.openbanking.ais.danske.oauth2.DanskeAuthMeansBuilderV3.CLIENT_ID_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 * This test contains all happy flows occurring in Danske provider.
 * Covered flows:
 * - acquiring consent page
 * - creating access means
 * - refreshing access means
 * - fetching accounts, balances, transactions
 * - create new registration on bank side using autoonboarding
 * - update existing registration on bank side using autoonboarding
 * <p>
 */
@SpringBootTest(classes = {DanskeApp.class, TestConfiguration.class, OpenbankingConfiguration.class}, webEnvironment = NONE)
@ActiveProfiles("danske")
@AutoConfigureWireMock(stubs = "classpath:/stubs/danske/ais-3.1.6/happyflow", httpsPort = 0, port = 0)
public class DanskeBankDataProviderHappyFlowIntegrationTest {

    private static final String REDIRECT_URL = "https://yolt.com/callback-test";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String SERIALIZED_ACCESS_MEANS = "{\"created\":\"2020-07-02T11:18:45.155037Z\",\"userId\":\"607713f9-e53e-48ab-8c60-2a9fe8217d53\",\"accessToken\":\"AccessToken\",\"refreshToken\":\"RefreshToken\",\"expireTime\":\"2020-07-02T11:28:44.993+0000\",\"updated\":\"2020-07-02T11:18:45.993+0000\",\"redirectUri\":\"https://www.yolt.com/callback/3651edaa-d36e-48cb-8cc3-94bb1fbe8f76\"}";
    private RestTemplateManagerMock restTemplateManagerMock;

    @Autowired
    private DanskeBankDataProviderV7 dataProvider;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    private final Signer signer = new SignerMock();

    @BeforeEach
    public void setup() throws IOException, URISyntaxException {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "551c6cce-fc16-49b9-8b16-ab66fea5eb35");
        authenticationMeans = new DanskeBankSampleTypedAuthenticationMeansV7().getAuthenticationMeans();
    }

    @Test
    public void shouldReturnConsentPageUrl() {
        // given
        String clientId = "2892ebea7ea9befa778897b5454fea56fb564fba4ebf65e4ba6546789fae98a9";
        String loginState = UUID.randomUUID().toString();

        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl("http://yolt.com/identifier").setState(loginState)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signer)
                .build();

        // when
        RedirectStep loginInfo = (RedirectStep) dataProvider.getLoginInfo(urlGetLogin);

        // then
        assertThat(loginInfo.getRedirectUrl()).contains("?response_type=code+id_token&client_id=" + clientId
                + "&state=" + loginState + "&scope=openid+accounts");
        assertThat(loginInfo.getRedirectUrl()).contains("&redirect_uri=http%3A%2F%2Fyolt.com%2Fidentifier&request=");
        assertThat(loginInfo.getExternalConsentId()).isEqualTo("11122233-317c-451c-8438-3b3fb91466e1");
    }

    @Test
    public void shouldCreateNewAccessMeans() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        String redirectUrl = "https://www.yolt.com/callback/3651edaa-d36e-48cb-8cc3-94bb1fbe8f76?code=authorization_code&state=secretState";

        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signer)
                .build();

        // when
        AccessMeansDTO newAccessMeans = dataProvider.createNewAccessMeans(urlCreateAccessMeans).getAccessMeans();

        // then
        assertThat(newAccessMeans.getUserId()).isEqualTo(userId);
        AccessMeans token = objectMapper.readValue(newAccessMeans.getAccessMeans(), AccessMeans.class);
        assertThat(token.getAccessToken()).isEqualTo("AccessToken");
    }

    @Test
    public void shouldRefreshAccessMeans() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        AccessMeansDTO accessMeans = new AccessMeansDTO(userId, SERIALIZED_ACCESS_MEANS, new Date(), new Date());
        UrlRefreshAccessMeansRequest urlRefreshAccessMeans = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        AccessMeansDTO retrievedAccessMeans = dataProvider.refreshAccessMeans(urlRefreshAccessMeans);

        // then
        AccessMeans deserializedOAuthToken = objectMapper.readValue(retrievedAccessMeans.getAccessMeans(), AccessMeans.class);
        assertThat(retrievedAccessMeans.getUserId()).isEqualTo(userId);
        assertThat(deserializedOAuthToken.getAccessToken()).isEqualTo("AccessToken");
        assertThat(deserializedOAuthToken.getRefreshToken()).isEqualTo("RefreshToken");
    }

    @Test
    public void shouldSuccessfullyFetchData() throws Exception {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, SERIALIZED_ACCESS_MEANS, new Date(),
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signer)
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts().size()).isEqualTo(1);
        dataProviderResponse.getAccounts().forEach(ProviderAccountDTO::validate);

        //Verify Current Account
        ProviderAccountDTO currentAccount = dataProviderResponse.getAccounts().get(0);
        validateCurrentAccount(currentAccount);
    }

    @Test
    void shouldCreateNewRegistration() {
        // given
        Map<String, BasicAuthenticationMean> registerMeans = new HashMap<>(authenticationMeans);
        registerMeans.remove(CLIENT_ID_NAME);

        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(registerMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(signer)
                .setRedirectUrls(Collections.singletonList(REDIRECT_URL))
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();

        // when
        Map<String, BasicAuthenticationMean> configureMeans = dataProvider.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(configureMeans).containsKey(CLIENT_ID_NAME);
        assertThat(configureMeans.get(CLIENT_ID_NAME).getValue()).isEqualTo("SOME_FAKE_CLIENT_ID");
    }

    @Test
    void shouldNotCreateNewRegistration() {
        // given
        Map<String, BasicAuthenticationMean> registerMeans = new HashMap<>(authenticationMeans);

        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequest(
                registerMeans,
                restTemplateManagerMock,
                signer,
                RegistrationOperation.CREATE,
                null,
                Collections.singletonList(REDIRECT_URL),
                Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS));

        // when
        Map<String, BasicAuthenticationMean> configureMeans = dataProvider.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(configureMeans).containsKey(CLIENT_ID_NAME);
        assertThat(configureMeans.get(CLIENT_ID_NAME).getValue()).isEqualTo("2892ebea7ea9befa778897b5454fea56fb564fba4ebf65e4ba6546789fae98a9");
    }

    @Test
    void shouldUpdateRegistration() {
        // given
        Map<String, BasicAuthenticationMean> registerMeans = new HashMap<>(authenticationMeans);

        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequest(
                registerMeans,
                restTemplateManagerMock,
                signer,
                RegistrationOperation.UPDATE,
                null,
                Collections.singletonList(REDIRECT_URL),
                Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS));

        // when
        Map<String, BasicAuthenticationMean> configureMeans = dataProvider.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(configureMeans).containsKey(CLIENT_ID_NAME);
        assertThat(configureMeans.get(CLIENT_ID_NAME).getValue()).isEqualTo("SOME_FAKE_CLIENT_ID");
    }

    private void validateCurrentAccount(ProviderAccountDTO providerAccountDTO) {
        assertThat(providerAccountDTO.getAccountId()).isEqualTo("77f5cb47-fb53-49e7-acd7-f8ed17ba4396");
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo("38.66");
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo("229.51");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAccountNumber().getIdentification()).isEqualTo("56037150203998");
        assertThat(providerAccountDTO.getAccountNumber().getScheme()).isEqualTo(ProviderAccountNumberDTO.Scheme.SORTCODEACCOUNTNUMBER);
        assertThat(providerAccountDTO.getName()).isEqualTo("Danske Bank Account");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);

        // Verify Standing Orders
        assertThat(providerAccountDTO.getStandingOrders().size()).isEqualTo(2);
        assertThat(providerAccountDTO.getStandingOrders().get(0).getDescription()).isEqualTo("Some Standing Order Reference 1");
        assertThat(providerAccountDTO.getStandingOrders().get(1).getDescription()).isEqualTo("Some Standing Order Reference 2");

        // Verify Direct Debits
        assertThat(providerAccountDTO.getDirectDebits().size()).isEqualTo(24);
        DirectDebitDTO directDebitDTO = providerAccountDTO.getDirectDebits().get(0);
        assertThat(directDebitDTO.getDirectDebitId()).isEqualTo("00eed4f2-f99e-4e59-b58b-7545d04bf23f");
        assertThat(directDebitDTO.getDescription()).isEqualTo("HASTINGS INSURANCE SERVICES LTD");
        assertThat(directDebitDTO.getPreviousPaymentAmount()).isEqualTo("69.51");
        assertThat(directDebitDTO.getPreviousPaymentDateTime()).isEqualTo("2020-11-30T00:00:00Z[Europe/London]");


        validateCurrentTransactions(providerAccountDTO.getTransactions());
    }

    private void validateCurrentTransactions(List<ProviderTransactionDTO> transactions) {
        assertThat(transactions.size()).isEqualTo(31);

        ProviderTransactionDTO pendingTransaction = transactions.get(0);
        assertThat(pendingTransaction.getAmount()).isEqualTo("1.80");
        assertThat(pendingTransaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(pendingTransaction.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(pendingTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        ExtendedTransactionDTO extendedTransaction = pendingTransaction.getExtendedTransaction();
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo("-1.80");
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("Cafe Mauds ))))");
        pendingTransaction.validate();

        ProviderTransactionDTO bookedTransaction = transactions.get(1);
        assertThat(bookedTransaction.getAmount()).isEqualTo("600.00");
        assertThat(bookedTransaction.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(bookedTransaction.getType()).isEqualTo(ProviderTransactionType.CREDIT);
        assertThat(bookedTransaction.getCategory()).isEqualTo(YoltCategory.GENERAL);
        extendedTransaction = bookedTransaction.getExtendedTransaction();
        assertThat(extendedTransaction.getTransactionAmount().getAmount()).isEqualTo("600.00");
        assertThat(extendedTransaction.getRemittanceInformationUnstructured()).isEqualTo("From Danske Freedom");
        bookedTransaction.validate();
    }
}
