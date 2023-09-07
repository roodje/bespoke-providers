package com.yolt.providers.openbanking.ais.tidegroup.ais;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.tidegroup.TideGroupApp;
import com.yolt.providers.openbanking.ais.tidegroup.TideGroupSampleTypedAuthMeansV2;
import com.yolt.providers.openbanking.ais.tidegroup.common.TideGroupDataProviderV2;
import com.yolt.providers.openbanking.ais.utils.UriHelper;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

/**
 * This test contains all happy flows occurring in Tide group providers.
 * Covered flows:
 * - acquiring consent page
 * - fetching accounts, balances, transactions, direct debits and standing orders
 * - creating access means
 * - refreshing authentication means
 * - deletion of consent
 * <p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {TideGroupApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("tidegroup")
@AutoConfigureWireMock(stubs = "classpath:/stubs/tidegroup/ob_3.1.1/ais/v2/happy_flow/", port = 0, httpsPort = 0)
public class TideGroupDataProviderV3HappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String EXTERNAL_CONSENT_ID = "650ac35750b8448db81cf77613dd62b5";
    private static final Signer SIGNER = new SignerMock();

    private static RestTemplateManagerMock restTemplateManagerMock;
    private static Map<String, BasicAuthenticationMean> authenticationMeans;
    private static String requestTraceId;

    @Autowired
    @Qualifier("TideDataProviderV3")
    private TideGroupDataProviderV2 tideDataProvider;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    private Stream<TideGroupDataProviderV2> getProviders() {
        return Stream.of(tideDataProvider);
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
        authenticationMeans = TideGroupSampleTypedAuthMeansV2.getAuthenticationMeans();
    }

    @BeforeEach
    void beforeEach() {
        requestTraceId = "12345";
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldGetLoginInfo(TideGroupDataProviderV2 subject) {
        // given
        String clientId = "someClientId";
        String loginState = UUID.randomUUID().toString();
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl("http://yolt.com/identifier").setState(loginState)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();

        // when
        RedirectStep loginInfo = (RedirectStep) subject.getLoginInfo(urlGetLogin);

        // then
        assertThat(loginInfo.getExternalConsentId()).isEqualTo(EXTERNAL_CONSENT_ID);
        assertThat(loginInfo.getRedirectUrl()).isNotEmpty();
        Map<String, String> queryParams = UriHelper.extractQueryParams(loginInfo.getRedirectUrl());
        assertThat(queryParams.get("client_id")).isEqualTo(clientId);
        assertThat(queryParams.get("state")).isEqualTo(loginState);
        assertThat(queryParams.get("response_type")).isEqualTo("code+id_token");
        assertThat(queryParams.get("scope")).isEqualTo("openid+accounts");
        assertThat(queryParams.get("nonce")).isEqualTo(loginState);
        assertThat(queryParams.get("redirect_uri")).isEqualTo("http%3A%2F%2Fyolt.com%2Fidentifier");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldFetchData(TideGroupDataProviderV2 subject) throws Exception {
        // given
        AccessMeans token = new AccessMeans(
                Instant.now(),
                null,
                "test-accounts",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                null,
                null);
        String serializedAccessMeans = objectMapper.writeValueAsString(token);
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, serializedAccessMeans, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        DataProviderResponse dataProviderResponse = subject.fetchData(urlFetchData);

        // then
        // Verify Current Account 1
        assertThat(dataProviderResponse.getAccounts()).hasSize(2);

        dataProviderResponse.getAccounts().stream().forEach(ProviderAccountDTO::validate);

        ProviderAccountDTO providerAccountDTO = dataProviderResponse.getAccounts().get(0);
        assertThat(providerAccountDTO.getName()).isEqualTo("Tide Current Account");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo("1230.00");
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo("57.36");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);

        // Verify Extended Account Balances
        assertThat(providerAccountDTO.getExtendedAccount().getBalances()).hasSize(3);

        // Verify Stand Order
        assertThat(providerAccountDTO.getStandingOrders()).hasSize(1);
        StandingOrderDTO standingOrderDTO = providerAccountDTO.getStandingOrders().get(0);
        assertThat(standingOrderDTO.getDescription()).isEqualTo("Towbar Club 2 - We Love Towbars");
        assertThat(standingOrderDTO.getFrequency()).isEqualTo(Period.ofDays(1));
        assertThat(standingOrderDTO.getNextPaymentAmount()).isEqualTo("0.56");
        assertThat(standingOrderDTO.getCounterParty().getIdentification()).isEqualTo("80200112345678");

        // Verify Direct Debit
        assertThat(providerAccountDTO.getDirectDebits()).hasSize(1);
        DirectDebitDTO directDebitDTO = providerAccountDTO.getDirectDebits().get(0);
        assertThat(directDebitDTO.getDescription()).isEqualTo("Towbar Club 3 - We Love Towbars");
        assertThat(directDebitDTO.isDirectDebitStatus()).isTrue();
        assertThat(directDebitDTO.getPreviousPaymentAmount()).isEqualTo("0.57");

        // Verify Current Account 2
        ProviderAccountDTO providerAccountDTO2 = dataProviderResponse.getAccounts().get(1);
        assertThat(providerAccountDTO2.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(providerAccountDTO2.getName()).isEqualTo("MOTEST-LBAAC2");
        assertThat(providerAccountDTO2.getTransactions()).hasSize(17);
        assertThat(providerAccountDTO2.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO2.getAvailableBalance()).isEqualTo("1230.00");
        assertThat(providerAccountDTO2.getCurrentBalance()).isEqualTo("-57.36");

        // Verify Extended Account Balances
        assertThat(providerAccountDTO2.getExtendedAccount().getBalances()).hasSize(2);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldRefreshAccessMeans(TideGroupDataProviderV2 subject) throws Exception {
        // given
        requestTraceId = "67890";
        AccessMeans oAuthToken = new AccessMeans(Instant.ofEpochMilli(0L),
                null,
                "test-accounts",
                "refreshToken",
                new Date(),
                null,
                null);
        String serializedOAuthToken = objectMapper.writeValueAsString(oAuthToken);
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, serializedOAuthToken, new Date(), new Date());
        UrlRefreshAccessMeansRequest urlRefreshAccessMeans = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setSigner(SIGNER)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        AccessMeansDTO retrievedAccessMeans = subject.refreshAccessMeans(urlRefreshAccessMeans);

        // then
        AccessMeans deserializedOAuthToken = objectMapper.readValue(retrievedAccessMeans.getAccessMeans(), AccessMeans.class);
        assertThat(retrievedAccessMeans.getUserId()).isEqualTo(USER_ID);
        assertThat(deserializedOAuthToken.getAccessToken()).isEqualTo("SOME_ACCESS_TOKEN");
        assertThat(deserializedOAuthToken.getRefreshToken()).isEqualTo("SOME_REFRESH_TOKEN");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldCreateNewAccessMeans(TideGroupDataProviderV2 subject) throws Exception {
        // given
        requestTraceId = "67890";
        UUID userId = UUID.randomUUID();
        String authorizationCode = "gktvoeyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiUlNBMV81Iiwia2lkIjoicTQtMjAxNy1tMi1CT1MifQ.JvophKQTiXv8tvE66jNaXidcYWw_a8BacizAdMiePt_Dd9zJAFU5-TN0qwVIwbIBWbc3hxmiz6VIyJjLoFVAb14QcJaBVuqAiv6Ci8Q752UA-R1aK-t3K1cT5iMtsGlO_7x2EfJum6ujZyCkeTQdKrdnYqH5r1VCLSLxlXFQedXUQ4xYOQr06b4Twj-APIH1dl6WKmIWTyvoFU6_FqGZVNFc_t8VE2KiUjnJnFyFlsF54077WFKiecSAzE_tOFqp0RN_eAaM8J4ycyBoO-cjJ3bJvBB3sXctoCG-lnSxQtP4c2eu0Qg6NIXpAiFEe562w0JRzW1d1ZFNjmBY4jGRIA.PAnSqNZdL4s539MyX4i-Rg.gepH1P5F_rrG5CCEMMkDQPRyxGcYdc136rVvwZs5sZS9kB9357PLJ7asdf8yeafjIKI-l-FoogsOvVf6dQE2_iVAmrTOoESGdk5szYvGC8_kSYmD8j2Kl9Px7xvjbaki-fW5wyR0F8c9MTRvT7aEx2JVy5RHq8hsMguAmCmTNi2NzyZXHhNoNxKmesYJpE2Bz-2bHBfWH1VakuhTp8751atBvbWvU97CMDbUAQx18QW4gL8pWaVtYfDx_5CfF6DP6Cv4RiK_NngCSV5CrdgcDhMWPZeeY41lVVITclG4-tpMZE3bp9W4NB2LYX_zShAR9OsnbD6qgHtwC_-6PfaPrNIW5PpTJK73IRzLxsU-bflLea4fHI2dtXSdL5msUqpM-kS-_tPBXweXT42AzIBNbIZ4Jj7R6WOhign5gx2Z_c3vj--1Pq2zh2ztZHwQ8s3oh5qUwkW_vrLG4ruL4MUDz_8MwTiTRNXZYRvq-M6fZAzN7B3_ykLHUbpoiGAl1Eli0Yw8N98WrcAfC6BWcwc2d-6hrwen6_QcZw0yX2nEt8bCRQwsbYoEE9PV3m38U0M3PAcqHkazVELJz4Afx_naFVRq6dlafQAuZbeS8kBF1gIhTubdWgQFEyCvIHvh5a_takLkDJimjrbYHsREykcrVdnJ73c_t4v6K5aWj7UOJ6p0w7nRjHBtV0uXlFJP-qfp.LZMdA6nFUbqat01P6uJFUA";
        String redirectUrl = "https://www.yolt.com/callback/5fe1e9f8-eb5f-4812-a6a6-2002759db545#code=" + authorizationCode + "&state=secretState";
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setUserId(userId)
                .setRedirectUrlPostedBackFromSite(redirectUrl)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(SIGNER)
                .build();

        // when
        AccessMeansDTO newAccessMeans = subject.createNewAccessMeans(urlCreateAccessMeans).getAccessMeans();

        // then
        assertThat(newAccessMeans.getUserId()).isEqualTo(userId);
        AccessMeans token = objectMapper.readValue(newAccessMeans.getAccessMeans(), AccessMeans.class);
        assertThat(token.getAccessToken()).isEqualTo("SOME_ACCESS_TOKEN");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldOnUserSiteDelete(TideGroupDataProviderV2 subject) {
        // given
        UrlOnUserSiteDeleteRequest urlOnUserSiteDelete = new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId(EXTERNAL_CONSENT_ID)
                .setSigner(SIGNER)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        ThrowableAssert.ThrowingCallable onUserSiteDelete = () -> subject.onUserSiteDelete(urlOnUserSiteDelete);

        // then
        assertThatCode(onUserSiteDelete)
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    public void shouldReturnCorrectlyFetchDataWhenAfterConsentWindow(TideGroupDataProviderV2 subject) throws Exception {
        // given
        AccessMeans token = new AccessMeans(
                Instant.now().minus(15, ChronoUnit.MINUTES),
                null,
                "test-accounts",
                "refreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                null,
                null);
        String serializedAccessMeans = objectMapper.writeValueAsString(token);
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, serializedAccessMeans, new Date(), Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        UrlFetchDataRequest urlFetchData = new UrlFetchDataRequestBuilder()
                .setTransactionsFetchStartTime(Instant.now())
                .setAccessMeans(accessMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        DataProviderResponse dataProviderResponse = subject.fetchData(urlFetchData);

        // then
        // Verify Current Account 1
        assertThat(dataProviderResponse.getAccounts()).hasSize(2);

        dataProviderResponse.getAccounts().stream().forEach(ProviderAccountDTO::validate);

        ProviderAccountDTO providerAccountDTO = dataProviderResponse.getAccounts().get(0);
        assertThat(providerAccountDTO.getName()).isEqualTo("Tide Current Account");
        assertThat(providerAccountDTO.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO.getAvailableBalance()).isEqualTo("1230.00");
        assertThat(providerAccountDTO.getCurrentBalance()).isEqualTo("57.36");
        assertThat(providerAccountDTO.getClosed()).isNull();
        assertThat(providerAccountDTO.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);

        // Verify Extended Account Balances
        assertThat(providerAccountDTO.getExtendedAccount().getBalances()).hasSize(3);

        // Verify Standing Orders - should be empty when after consent window
        assertThat(providerAccountDTO.getStandingOrders()).isEmpty();

        // Verify Direct - should be empty when after consent window
        assertThat(providerAccountDTO.getDirectDebits()).isEmpty();

        // Verify Current Account 2
        ProviderAccountDTO providerAccountDTO2 = dataProviderResponse.getAccounts().get(1);
        assertThat(providerAccountDTO2.getYoltAccountType()).isEqualTo(AccountType.CURRENT_ACCOUNT);
        assertThat(providerAccountDTO2.getName()).isEqualTo("MOTEST-LBAAC2");
        assertThat(providerAccountDTO2.getTransactions()).hasSize(17);
        assertThat(providerAccountDTO2.getCurrency()).isEqualTo(CurrencyCode.GBP);
        assertThat(providerAccountDTO2.getAvailableBalance()).isEqualTo("1230.00");
        assertThat(providerAccountDTO2.getCurrentBalance()).isEqualTo("-57.36");

        // Verify Extended Account Balances
        assertThat(providerAccountDTO2.getExtendedAccount().getBalances()).hasSize(2);
    }
}
