package com.yolt.providers.openbanking.ais.rbsgroup.ais.v11;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsApp;
import com.yolt.providers.openbanking.ais.rbsgroup.RbsSampleAuthenticationMeansV4;
import com.yolt.providers.openbanking.ais.rbsgroup.common.RbsGroupDataProviderV5;
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
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

/**
 * This test contains all authentication happy flows occurring in RBS group providers.
 * <p>
 * Covered flows:
 * - acquiring consent page
 * - creating access means
 * - refreshing authentication means
 * - deletion of consent
 * <p>
 * Providers: ALL RBS Group
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = RbsApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("rbsgroup-v5")
@AutoConfigureWireMock(stubs = "classpath:/stubs/rbsgroup/ob_3.1.6/ais/happy_flow/authentication", port = 0, httpsPort = 0)
class RbsGroupDataProviderAuthenticationHappyFlowIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String EXTERNAL_CONSENT_ID = "650ac35750b8448db81cf77613dd62b5";

    private static RestTemplateManagerMock restTemplateManagerMock;
    private static Map<String, BasicAuthenticationMean> authenticationMeans;
    private static String requestTraceId;

    private Signer signer;

    @Autowired
    @Qualifier("CouttsDataProviderV3")
    private RbsGroupDataProviderV5 couttsDataProvider;
    @Autowired
    @Qualifier("NatWestDataProviderV11")
    private RbsGroupDataProviderV5 natwestDataProvider;
    @Autowired
    @Qualifier("NatWestCorporateDataProviderV10")
    private RbsGroupDataProviderV5 natwestCorpoDataProvider;
    @Autowired
    @Qualifier("RoyalBankOfScotlandDataProviderV11")
    private RbsGroupDataProviderV5 rbsDataProvider;
    @Autowired
    @Qualifier("RoyalBankOfScotlandCorporateDataProviderV10")
    private RbsGroupDataProviderV5 rbsCorpoDataProvider;
    @Autowired
    @Qualifier("UlsterBankDataProviderV10")
    private RbsGroupDataProviderV5 ulsterDataProvider;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    private Stream<RbsGroupDataProviderV5> getProviders() {
        return Stream.of(
                natwestDataProvider,
                natwestCorpoDataProvider,
                rbsDataProvider,
                rbsCorpoDataProvider,
                ulsterDataProvider,
                couttsDataProvider);
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        restTemplateManagerMock = new RestTemplateManagerMock(() -> requestTraceId);
        authenticationMeans = RbsSampleAuthenticationMeansV4.getRbsSampleAuthenticationMeansForAis();
    }

    @BeforeEach
    void beforeEach() {
        requestTraceId = "12345";
        signer = new SignerMock();
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldGetLoginInfo(RbsGroupDataProviderV5 subject) {
        // given
        String clientId = "someClientId";
        String loginState = UUID.randomUUID().toString();
        UrlGetLoginRequest urlGetLogin = new UrlGetLoginRequestBuilder()
                .setBaseClientRedirectUrl("http://yolt.com/identifier").setState(loginState)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .setSigner(signer)
                .build();

        // when
        RedirectStep loginInfo = (RedirectStep) subject.getLoginInfo(urlGetLogin);

        // then
        String expectedUrlRegex = ".*\\/authorize\\?response_type=code\\+id_token&client_id=" + clientId + "&state=" + loginState + "&scope=openid\\+accounts&nonce=" + loginState + "&redirect_uri=http%3A%2F%2Fyolt.com%2Fidentifier&request=.*$";
        assertThat(loginInfo.getRedirectUrl()).matches(expectedUrlRegex);
        assertThat(loginInfo.getExternalConsentId()).isEqualTo(EXTERNAL_CONSENT_ID);
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldRefreshAccessMeans(RbsGroupDataProviderV5 subject) throws Exception {
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
        UrlRefreshAccessMeansRequest urlRefreshAccessMeans = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(new AccessMeansDTO(USER_ID, serializedOAuthToken, new Date(), new Date()))
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        AccessMeansDTO accessMeansDTO = subject.refreshAccessMeans(urlRefreshAccessMeans);

        // then
        assertThat(accessMeansDTO.getUserId()).isEqualTo(USER_ID);

        AccessMeans accessMeans = objectMapper.readValue(accessMeansDTO.getAccessMeans(), AccessMeans.class);
        assertThat(accessMeans.getAccessToken()).isEqualTo("SOME_ACCESS_TOKEN");
        assertThat(accessMeans.getRefreshToken()).isEqualTo("SOME_REFRESH_TOKEN");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldCreateNewAccessMeans(RbsGroupDataProviderV5 subject) throws JsonProcessingException {
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
                .setSigner(signer)
                .build();

        // when
        AccessMeansDTO accessMeansDTO = subject.createNewAccessMeans(urlCreateAccessMeans).getAccessMeans();

        // then
        assertThat(accessMeansDTO.getUserId()).isEqualTo(userId);

        AccessMeans accessMeans = objectMapper.readValue(accessMeansDTO.getAccessMeans(), AccessMeans.class);
        assertThat(accessMeans.getAccessToken()).isEqualTo("SOME_ACCESS_TOKEN");
        assertThat(accessMeans.getRefreshToken()).isEqualTo("SOME_REFRESH_TOKEN");
        assertThat(accessMeans.getExpireTime()).isAfter(new Date());
        assertThat(accessMeans.getUpdated()).isCloseTo(new Date(), 5000);
        assertThat(accessMeans.getRedirectUri()).isEqualTo("https://www.yolt.com/callback/5fe1e9f8-eb5f-4812-a6a6-2002759db545");
    }

    @ParameterizedTest
    @MethodSource("getProviders")
    void shouldOnUserSiteDelete(RbsGroupDataProviderV5 subject) {
        // given
        UrlOnUserSiteDeleteRequest urlOnUserSiteDelete = new UrlOnUserSiteDeleteRequestBuilder()
                .setExternalConsentId(EXTERNAL_CONSENT_ID)
                .setSigner(signer)
                .setRestTemplateManager(restTemplateManagerMock)
                .setAuthenticationMeans(authenticationMeans)
                .build();

        // when
        ThrowableAssert.ThrowingCallable onUserSiteDelete = () -> subject.onUserSiteDelete(urlOnUserSiteDelete);

        // then
        assertThatCode(onUserSiteDelete)
                .doesNotThrowAnyException();
    }
}
