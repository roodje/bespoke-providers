package com.yolt.providers.openbanking.ais.sainsburys;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.*;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.yolt.providers.openbanking.ais.sainsburys.auth.SainsburysAuthMeansMapperV2.CLIENT_ID_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * This test contains all happy flows occurring in Sainsburys provider.
 * Covered flows:
 * - acquiring consent page
 * - fetching accounts, balances, transactions
 * - creating access means
 * - refreshing access means
 * - create new registration on bank side using autoonboarding
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {SainsburysApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/sainsburys/ais-3.1.1/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("sainsburys")
public class SainsburysDataProviderV2HappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";
    private static final Signer SIGNER = new SignerMock();
    private static final ZoneId zoneId = ZoneId.of("Europe/London");
    private static String SERIALIZED_ACCESS_MEANS;

    private static RestTemplateManagerMock restTemplateManagerMock;

    @Autowired
    @Qualifier("SainsburysObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("SainsburysDataProviderV2")
    private SainsburysBaseDataProvider dataProvider;

    private static Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeAll
    public void setup() throws IOException, URISyntaxException {
        AccessMeans accessToken = new AccessMeans(
                Instant.now(),
                USER_ID,
                "accessToken",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL);

        SERIALIZED_ACCESS_MEANS = objectMapper.writeValueAsString(accessToken);
        authenticationMeans = SainsburysSampleTypedAuthMeansV2.getAuthenticationMeans();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "4bf28754-9c17-41e6-bc46-6cf98fff6795");
    }

    @Test
    public void shouldReturnConsentPageUrl() {
        // given
        String clientId = "someClientId";
        String loginState = UUID.randomUUID().toString();
        String expectedUrlRegex = "?response_type=code+id_token&client_id=" + clientId + "&state=" +
                loginState + "&scope=openid+accounts&nonce=" + loginState + "&redirect_uri=http%3A%2F%2Fyolt.com%2Fidentifier&request=";

        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl("http://yolt.com/identifier").setState(loginState)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();

        // when
        RedirectStep loginInfo = (RedirectStep) dataProvider.getLoginInfo(urlGetLogin);

        // then
        assertThat(loginInfo.getRedirectUrl()).contains(expectedUrlRegex);
        assertThat(loginInfo.getExternalConsentId()).isEqualTo("50ca5ed5-317c-451c-8438-3b3fb91466e1");
    }

    @Test
    public void shouldCreateNewAccessMeans() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        String authorizationCode = "gktvoeyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiUlNBMV81Iiwia2lkIjoicTQtMjAxNy1tMi1CT1MifQ.JvophKQTiXv8tvE66jNaXidcYWw_a8BacizAdMiePt_Dd9zJAFU5-TN0qwVIwbIBWbc3hxmiz6VIyJjLoFVAb14QcJaBVuqAiv6Ci8Q752UA-R1aK-t3K1cT5iMtsGlO_7x2EfJum6ujZyCkeTQdKrdnYqH5r1VCLSLxlXFQedXUQ4xYOQr06b4Twj-APIH1dl6WKmIWTyvoFU6_FqGZVNFc_t8VE2KiUjnJnFyFlsF54077WFKiecSAzE_tOFqp0RN_eAaM8J4ycyBoO-cjJ3bJvBB3sXctoCG-lnSxQtP4c2eu0Qg6NIXpAiFEe562w0JRzW1d1ZFNjmBY4jGRIA.PAnSqNZdL4s539MyX4i-Rg.gepH1P5F_rrG5CCEMMkDQPRyxGcYdc136rVvwZs5sZS9kB9357PLJ7asdf8yeafjIKI-l-FoogsOvVf6dQE2_iVAmrTOoESGdk5szYvGC8_kSYmD8j2Kl9Px7xvjbaki-fW5wyR0F8c9MTRvT7aEx2JVy5RHq8hsMguAmCmTNi2NzyZXHhNoNxKmesYJpE2Bz-2bHBfWH1VakuhTp8751atBvbWvU97CMDbUAQx18QW4gL8pWaVtYfDx_5CfF6DP6Cv4RiK_NngCSV5CrdgcDhMWPZeeY41lVVITclG4-tpMZE3bp9W4NB2LYX_zShAR9OsnbD6qgHtwC_-6PfaPrNIW5PpTJK73IRzLxsU-bflLea4fHI2dtXSdL5msUqpM-kS-_tPBXweXT42AzIBNbIZ4Jj7R6WOhign5gx2Z_c3vj--1Pq2zh2ztZHwQ8s3oh5qUwkW_vrLG4ruL4MUDz_8MwTiTRNXZYRvq-M6fZAzN7B3_ykLHUbpoiGAl1Eli0Yw8N98WrcAfC6BWcwc2d-6hrwen6_QcZw0yX2nEt8bCRQwsbYoEE9PV3m38U0M3PAcqHkazVELJz4Afx_naFVRq6dlafQAuZbeS8kBF1gIhTubdWgQFEyCvIHvh5a_takLkDJimjrbYHsREykcrVdnJ73c_t4v6K5aWj7UOJ6p0w7nRjHBtV0uXlFJP-qfp.LZMdA6nFUbqat01P6uJFUA";
        String redirectUrl = "https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0?code=" + authorizationCode + "&state=secretState";
        Date _29MinutesFromNow = Date.from(Instant.now().plus(4, ChronoUnit.MINUTES));

        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();

        // when
        AccessMeansDTO newAccessMeans = dataProvider.createNewAccessMeans(urlCreateAccessMeans).getAccessMeans();

        // then
        assertThat(_29MinutesFromNow).isBefore(newAccessMeans.getExpireTime());
        assertThat(newAccessMeans.getUserId()).isEqualTo(userId);
        AccessMeans token = objectMapper.readValue(newAccessMeans.getAccessMeans(), AccessMeans.class);
        assertThat(token.getAccessToken()).isEqualTo("SOME_ACCESS_TOKEN");
        assertThat(_29MinutesFromNow).isBefore(token.getExpireTime());
    }

    @Test
    public void shouldCorrectlyRefreshAccessMeans() throws Exception {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(UUID.randomUUID(), "my_access_means",
                new Date(),
                new Date());
        AccessMeans token = new AccessMeans(
                USER_ID,
                "accessToken",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL);
        String serializedToken = objectMapper.writeValueAsString(token);
        accessMeans.setAccessMeans(serializedToken);
        accessMeans.setUserId(USER_ID);

        UrlRefreshAccessMeansRequest urlRefreshAccessMeansRequest = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .build();

        // when
        AccessMeansDTO resultAccessMeans = dataProvider.refreshAccessMeans(urlRefreshAccessMeansRequest);

        // then
        AccessMeans deserializedOAuthToken = objectMapper.readValue(resultAccessMeans.getAccessMeans(), AccessMeans.class);

        assertThat(resultAccessMeans.getUserId()).isEqualTo(USER_ID);
        assertThat(deserializedOAuthToken.getAccessToken()).isEqualTo("SOME_ACCESS_TOKEN");
        assertThat(deserializedOAuthToken.getRefreshToken()).isEqualTo("refreshToken");
    }

    @Test
    public void shouldCorrectlyFetchData() throws Exception {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, SERIALIZED_ACCESS_MEANS, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(4);
        dataProviderResponse.getAccounts().forEach(ProviderAccountDTO::validate);

        ProviderAccountDTO creditCardAccount = dataProviderResponse.getAccounts().get(0);
        validateCreditCardAccount(creditCardAccount);
        validateCreditCardTransactions(creditCardAccount.getTransactions());
        creditCardAccount = dataProviderResponse.getAccounts().get(1);
        assertThat(creditCardAccount.getName()).isEqualTo("Sainsbury's Bank Account");
    }

    @Test
    public void shouldCorrectlyAutoOnboarding() {
        // given
        Map<String, BasicAuthenticationMean> registerMeans = new HashMap<>(authenticationMeans);
        registerMeans.remove(CLIENT_ID_NAME);

        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(registerMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .setRedirectUrls(Collections.singletonList(TEST_REDIRECT_URL))
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();

        // when
        Map<String, BasicAuthenticationMean> configureMeans = dataProvider.autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(configureMeans.containsKey(CLIENT_ID_NAME)).isTrue();
    }

    @Test
    public void shouldCorrectlyRemoveAutoConfiguration() {
        // given
        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setSigner(SIGNER)
                .setRedirectUrls(Collections.singletonList(TEST_REDIRECT_URL))
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> dataProvider.removeAutoConfiguration(urlAutoOnboardingRequest);

        // then
        assertThatCode(throwingCallable)
                .doesNotThrowAnyException();
    }

    private void validateCreditCardAccount(ProviderAccountDTO providerAccountDTO) {
        ExtendedAccountDTO extendedAccount = providerAccountDTO.getExtendedAccount();
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo("6608.50");
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo("-2056.50");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getName()).isEqualTo("Credit Card Name");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CREDIT_CARD);
        assertThat(extendedAccount.getAccountReferences()).contains(
                AccountReferenceDTO.builder().type(AccountReferenceType.PAN).value("6961").build()
        );
        assertThat(extendedAccount.getBalances()).hasSameElementsAs(getExtendedBalancesForCreditCard());
    }

    private void validateCreditCardTransactions(final List<ProviderTransactionDTO> transactions) {
        assertThat(transactions).hasSize(5);

        ProviderTransactionDTO transaction1 = transactions.get(0);
        assertThat(transaction1.getAmount()).isEqualTo("2.37");
        assertThat(transaction1.getStatus()).isEqualTo(TransactionStatus.BOOKED);
        assertThat(transaction1.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction1.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction1.getMerchant()).isEqualTo("CRV*GOOGLE Google Pla London");

        transaction1.validate();

        ProviderTransactionDTO transaction2 = transactions.get(1);
        assertThat(transaction2.getAmount()).isEqualTo("16.80");
        assertThat(transaction2.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction2.getType()).isEqualTo(ProviderTransactionType.DEBIT);
        assertThat(transaction2.getCategory()).isEqualTo(YoltCategory.GENERAL);
        assertThat(transaction2.getMerchant()).isEqualTo("CRV*HAND PULLED PUB CO London");

        transaction2.validate();
    }

    private List<BalanceDTO> getExtendedBalancesForCreditCard() {
        List<BalanceDTO> balanceList = new ArrayList<>();
        balanceList.add(BalanceDTO.builder()
                .balanceType(BalanceType.INTERIM_CLEARED)
                .balanceAmount(new BalanceAmountDTO(CurrencyCode.GBP, new BigDecimal("-2056.50")))
                .lastChangeDateTime(ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2020-08-11T13:13Z")).withZoneSameInstant(zoneId))
                .referenceDate(ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse("2020-08-11T13:13Z")).withZoneSameInstant(zoneId))
                .build());
        return balanceList;
    }
}
