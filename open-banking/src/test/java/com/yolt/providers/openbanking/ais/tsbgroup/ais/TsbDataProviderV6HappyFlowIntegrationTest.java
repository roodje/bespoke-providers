package com.yolt.providers.openbanking.ais.tsbgroup.ais;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.AutoOnboardingProvider;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.tsbgroup.TsbGroupApp;
import com.yolt.providers.openbanking.ais.tsbgroup.TsbGroupSampleTypedAuthenticationMeans;
import com.yolt.providers.openbanking.ais.tsbgroup.common.TsbGroupBaseDataProvider;
import com.yolt.providers.openbanking.ais.tsbgroup.common.auth.TsbGroupAuthMeansBuilderV3;
import com.yolt.providers.openbanking.ais.utils.OpenBankingTestObjectMapper;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.TokenScope;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeAll;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test contains all happy flows occurring in TSB provider.
 * <p>
 * Disclaimer: as all providers in TSB group are the same from code and stubs perspective (then only difference is configuration)
 * we are running parametrized tests for testing, but this covers all providers from TSB group.
 * <p>
 * Covered flows:
 * - acquiring consent page
 * - creating access means
 * - refreshing access means
 * - fetching accounts, balances, transactions
 * - create new registration on bank side using autoonboarding
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {TsbGroupApp.class, OpenbankingConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/tsbgroup/ais/ob_3.1.1/happy-flow", httpsPort = 0, port = 0)
@ActiveProfiles("tsbgroup")
public class TsbDataProviderV6HappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID USER_SITE_ID = UUID.randomUUID();
    private static String SERIALIZED_ACCESS_MEANS;
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";
    private static RestTemplateManagerMock REST_TEMPLATE_MANAGER;
    private static Map<String, BasicAuthenticationMean> AUTH_MEANS;
    private static final Signer SIGNER = new SignerMock();

    private final ObjectMapper objectMapper = OpenBankingTestObjectMapper.INSTANCE;
    @Autowired
    @Qualifier("TsbDataProviderV6")
    private TsbGroupBaseDataProvider tsbDataProviderV6;
    @BeforeAll
    public static void setup() throws IOException, URISyntaxException {
        AccessMeans token = new AccessMeans(
                Instant.now(),
                USER_ID,
                "accessToken",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL);

        SERIALIZED_ACCESS_MEANS = OpenBankingTestObjectMapper.INSTANCE.writeValueAsString(token);
        AUTH_MEANS = new TsbGroupSampleTypedAuthenticationMeans().getAuthenticationMeans();
        REST_TEMPLATE_MANAGER = new RestTemplateManagerMock(() -> "87da2798-f7e2-4823-80c1-3c03344b8f13");
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnConsentPageUrl(UrlDataProvider dataProvider) {
        // given
        String clientId = "someClientId";
        String loginState = "97a6207e-01ad-41e9-9e13-3d67f8c5194e";
        String expectedUrlRegex = "?response_type=code+id_token&client_id=" + clientId + "&state=" +
                loginState + "&scope=openid+accounts&nonce=" + loginState + "&redirect_uri=http%3A%2F%2Fyolt.com%2Fidentifier&request=";

        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl("http://yolt.com/identifier").setState(loginState)
                .setAuthenticationMeans(AUTH_MEANS)
                .setExternalConsentId(null)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER)
                .setSigner(new SignerMock())
                .build();

        // when
        RedirectStep loginInfo = (RedirectStep) dataProvider.getLoginInfo(urlGetLogin);

        // then
        assertThat(loginInfo.getRedirectUrl()).contains(expectedUrlRegex);
        assertThat(loginInfo.getExternalConsentId()).isEqualTo("50ca5ed5-317c-451c-8438-3b3fb91466e1");
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldCreateNewAccessMeans(UrlDataProvider dataProvider) throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        String authorizationCode = "gktvoeyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiUlNBMV81Iiwia2lkIjoicTQtMjAxNy1tMi1CT1MifQ.JvophKQTiXv8tvE66jNaXidcYWw_a8BacizAdMiePt_Dd9zJAFU5-TN0qwVIwbIBWbc3hxmiz6VIyJjLoFVAb14QcJaBVuqAiv6Ci8Q752UA-R1aK-t3K1cT5iMtsGlO_7x2EfJum6ujZyCkeTQdKrdnYqH5r1VCLSLxlXFQedXUQ4xYOQr06b4Twj-APIH1dl6WKmIWTyvoFU6_FqGZVNFc_t8VE2KiUjnJnFyFlsF54077WFKiecSAzE_tOFqp0RN_eAaM8J4ycyBoO-cjJ3bJvBB3sXctoCG-lnSxQtP4c2eu0Qg6NIXpAiFEe562w0JRzW1d1ZFNjmBY4jGRIA.PAnSqNZdL4s539MyX4i-Rg.gepH1P5F_rrG5CCEMMkDQPRyxGcYdc136rVvwZs5sZS9kB9357PLJ7asdf8yeafjIKI-l-FoogsOvVf6dQE2_iVAmrTOoESGdk5szYvGC8_kSYmD8j2Kl9Px7xvjbaki-fW5wyR0F8c9MTRvT7aEx2JVy5RHq8hsMguAmCmTNi2NzyZXHhNoNxKmesYJpE2Bz-2bHBfWH1VakuhTp8751atBvbWvU97CMDbUAQx18QW4gL8pWaVtYfDx_5CfF6DP6Cv4RiK_NngCSV5CrdgcDhMWPZeeY41lVVITclG4-tpMZE3bp9W4NB2LYX_zShAR9OsnbD6qgHtwC_-6PfaPrNIW5PpTJK73IRzLxsU-bflLea4fHI2dtXSdL5msUqpM-kS-_tPBXweXT42AzIBNbIZ4Jj7R6WOhign5gx2Z_c3vj--1Pq2zh2ztZHwQ8s3oh5qUwkW_vrLG4ruL4MUDz_8MwTiTRNXZYRvq-M6fZAzN7B3_ykLHUbpoiGAl1Eli0Yw8N98WrcAfC6BWcwc2d-6hrwen6_QcZw0yX2nEt8bCRQwsbYoEE9PV3m38U0M3PAcqHkazVELJz4Afx_naFVRq6dlafQAuZbeS8kBF1gIhTubdWgQFEyCvIHvh5a_takLkDJimjrbYHsREykcrVdnJ73c_t4v6K5aWj7UOJ6p0w7nRjHBtV0uXlFJP-qfp.LZMdA6nFUbqat01P6uJFUA";
        String redirectUrl = "https://www.yolt.com/callback/68eef1a1-0b13-4d4b-9cc2-09a8b2604ca0?code=" + authorizationCode + "&state=secretState";
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setAuthenticationMeans(AUTH_MEANS)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER)
                .build();

        // when
        final AccessMeansOrStepDTO accessMeansStep = dataProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        AccessMeansDTO newAccessMeans = accessMeansStep.getAccessMeans();
        Date _29MinutesFromNow = Date.from(Instant.now().plus(4, ChronoUnit.MINUTES));
        assertThat(_29MinutesFromNow.before(newAccessMeans.getExpireTime())).isTrue();
        assertThat(newAccessMeans.getUserId()).isEqualTo(userId);
        AccessMeans token = objectMapper.readValue(newAccessMeans.getAccessMeans(), AccessMeans.class);
        assertThat(token.getAccessToken()).isEqualTo("SOME_ACCESS_TOKEN");
        assertThat(_29MinutesFromNow.before(token.getExpireTime())).isTrue();
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldCorrectlyRefreshAccessMeans(UrlDataProvider dataProvider) throws Exception {
        // given
        ObjectMapper objectMapper = OpenBankingTestObjectMapper.INSTANCE;

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
                .setAuthenticationMeans(AUTH_MEANS)
                .setSigner(SIGNER)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER)
                .build();

        // when
        AccessMeansDTO resultAccessMeans = dataProvider.refreshAccessMeans(urlRefreshAccessMeansRequest);

        // then
        AccessMeans deserializedOAuthToken = OpenBankingTestObjectMapper.INSTANCE.readValue(resultAccessMeans.getAccessMeans(), AccessMeans.class);

        assertThat(resultAccessMeans.getUserId()).isEqualTo(USER_ID);
        assertThat(deserializedOAuthToken.getAccessToken()).isEqualTo("SOME_ACCESS_TOKEN");
        assertThat(deserializedOAuthToken.getRefreshToken()).isEqualTo("SOME_REFRESH_TOKEN");
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    public void shouldReturnCorrectlyFetchData(UrlDataProvider dataProvider) throws TokenInvalidException, ProviderFetchDataException {
        // given
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setUserId(USER_ID)
                .setUserSiteId(USER_SITE_ID)
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(USER_ID, SERIALIZED_ACCESS_MEANS, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))
                .setAuthenticationMeans(AUTH_MEANS)
                .setSigner(SIGNER)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER)
                .build();

        // when
        DataProviderResponse dataProviderResponse = dataProvider.fetchData(urlFetchData);

        // then
        assertThat(dataProviderResponse.getAccounts()).hasSize(4);
        dataProviderResponse.getAccounts().forEach(ProviderAccountDTO::validate);

        Optional<ProviderAccountDTO> firstAccount = dataProviderResponse.getAccounts()
                .stream().filter(
                        account ->
                                account.getAccountId().equalsIgnoreCase("9e1b14ba-5923-4ba2-bf2a-e910b1d3438e"))
                .findFirst();
        assertThat(firstAccount.isPresent()).isTrue();
        ProviderAccountDTO providerAccountDTO = firstAccount.get();
        assertThat(providerAccountDTO.getName()).isEqualTo("Current Account");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo(new BigDecimal("1.00"));
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo(new BigDecimal("2.00"));
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);

        // Verify Stand Order
        assertThat(providerAccountDTO.getStandingOrders()).hasSize(0);

        // Verify Direct Debit
        assertThat(providerAccountDTO.getDirectDebits()).hasSize(0);
    }

    @ParameterizedTest
    @MethodSource("getDataProviders")
    void shouldRegister(UrlDataProvider dataProvider) throws IOException, URISyntaxException {
        // given
        Map<String, BasicAuthenticationMean> registerMeans = new HashMap<>(
                TsbGroupSampleTypedAuthenticationMeans.getTsbGroupSampleTypedAuthenticationMeansForFcaRegistration());

        UrlAutoOnboardingRequest urlAutoOnboardingRequest = new UrlAutoOnboardingRequestBuilder()
                .setAuthenticationMeans(registerMeans)
                .setRestTemplateManager(REST_TEMPLATE_MANAGER)
                .setSigner(SIGNER)
                .setRedirectUrls(Collections.singletonList(TEST_REDIRECT_URL))
                .setScopes(Set.of(TokenScope.ACCOUNTS, TokenScope.PAYMENTS))
                .build();

        // when
        Map<String, BasicAuthenticationMean> configureMeans = ((AutoOnboardingProvider) dataProvider).autoConfigureMeans(urlAutoOnboardingRequest);

        // then
        assertThat(configureMeans.containsKey(TsbGroupAuthMeansBuilderV3.CLIENT_ID_NAME)).isTrue();
        assertThat(configureMeans.get(TsbGroupAuthMeansBuilderV3.CLIENT_ID_NAME).getValue()).isEqualTo("SOME_FAKE_CLIENT_ID");
        assertThat(configureMeans.containsKey(TsbGroupAuthMeansBuilderV3.CLIENT_SECRET_NAME)).isTrue();
        assertThat(configureMeans.get(TsbGroupAuthMeansBuilderV3.CLIENT_SECRET_NAME).getValue()).isEqualTo("SOME_FAKE_CLIENT_SECRET");
    }

    private Stream<UrlDataProvider> getDataProviders() {
        return Stream.of(tsbDataProviderV6);
    }
}
