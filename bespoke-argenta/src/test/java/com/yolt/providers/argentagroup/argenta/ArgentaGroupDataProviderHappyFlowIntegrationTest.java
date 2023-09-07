package com.yolt.providers.argentagroup.argenta;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.argentagroup.ArgentaGroupTestApp;
import com.yolt.providers.argentagroup.SignerMock;
import com.yolt.providers.argentagroup.common.service.token.AccessMeans;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.*;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;


@ActiveProfiles("argenta")
@SpringBootTest(classes = {ArgentaGroupTestApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = {"classpath:/stubs/argenta/ais-1.5/happy-flow"}, httpsPort = 0, port = 0)
class ArgentaGroupDataProviderHappyFlowIntegrationTest {

    private static final String TEST_PSU_IP_ADDRESS = "192.168.0.1";
    private static final String TEST_REDIRECT_URL = "https://www.yolt.com/callback";
    private static final String TEST_STATE = UUID.randomUUID().toString();
    private static final Signer SIGNER = new SignerMock();
    private static final String TEST_CONSENT_ID = "TEST_CONSENT_ID";
    private static final Instant TEST_TRANSACTION_FETCH_START_TIME = Instant.parse("2020-12-31T00:00:00Z");

    @Autowired
    private Clock clock;

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("argentaDataProviderV1")
    private UrlDataProvider dataProvider;

    @Test
    void shouldReturnConsentPageUrl() throws IOException, URISyntaxException {
        // given
        var request = new UrlGetLoginRequestBuilder()
                .setAuthenticationMeans(ArgentaAuthenticationMeansFixtures.getSampleAuthenticationMeans())
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .setBaseClientRedirectUrl(TEST_REDIRECT_URL)
                .setSigner(SIGNER)
                .setRestTemplateManager(restTemplateManager)
                .setState(TEST_STATE)
                .build();

        // when
        var result = (RedirectStep) dataProvider.getLoginInfo(request);

        // then
        var redirectUrlQueryParams = UriComponentsBuilder
                .fromHttpUrl(result.getRedirectUrl())
                .build()
                .getQueryParams()
                .toSingleValueMap();

        assertThat(redirectUrlQueryParams)
                .containsEntry("response_type", "code")
                .containsEntry("client_id", "TEST_CLIENT_ID")
                .containsEntry("scope", "AIS:" + TEST_CONSENT_ID)
                .containsEntry("redirect_uri", TEST_REDIRECT_URL)
                .containsEntry("state", TEST_STATE)
                .containsEntry("code_challenge_method", "S256")
                .containsKey("code_challenge");
    }

    @Test
    void shouldCreateNewAccessMeans() throws IOException, URISyntaxException {
        // given
        var testUserId = UUID.randomUUID();
        var redirectUrlPostedBackFromSite = "https://www.yolt.com/callback?code=TEST_AUTHORIZATION_CODE";

        var request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(ArgentaAuthenticationMeansFixtures.getSampleAuthenticationMeans())
                .setBaseClientRedirectUrl(TEST_REDIRECT_URL)
                .setRestTemplateManager(restTemplateManager)
                .setState(TEST_STATE)
                .setUserId(testUserId)
                .setRedirectUrlPostedBackFromSite(redirectUrlPostedBackFromSite)
                .setProviderState(getProviderState())
                .build();

        // when
        var result = dataProvider.createNewAccessMeans(request);
        var accessMeans = objectMapper.readValue(result.getAccessMeans().getAccessMeans(), AccessMeans.class);

        // then
        assertThat(result.getAccessMeans().getUserId()).isEqualTo(testUserId);
        assertThat(accessMeans.getScope()).isEqualTo("AIS");
        assertThat(accessMeans.getAccessToken()).isEqualTo("TEST_ACCESS_TOKEN");
        assertThat(accessMeans.getRefreshToken()).isEqualTo("TEST_REFRESH_TOKEN");
        assertThat(accessMeans.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(accessMeans.getExpiresIn()).isEqualTo(599L);
    }

    @Test
    void shouldRefreshAccessMeans() throws IOException, URISyntaxException, TokenInvalidException {
        // given
        var testUserId = UUID.randomUUID();

        var request = new UrlRefreshAccessMeansRequestBuilder()
                .setAuthenticationMeans(ArgentaAuthenticationMeansFixtures.getSampleAuthenticationMeans())
                .setRestTemplateManager(restTemplateManager)
                .setAccessMeans(testUserId, getSerializedAccessMeans(), new Date(), new Date())
                .build();

        // when
        var result = dataProvider.refreshAccessMeans(request);
        var accessMeans = objectMapper.readValue(result.getAccessMeans(), AccessMeans.class);

        // then
        assertThat(result.getUserId()).isEqualTo(testUserId);
        assertThat(accessMeans.getAccessToken()).isEqualTo("TEST_REFRESHED_ACCESS_TOKEN");
        assertThat(accessMeans.getRefreshToken()).isEqualTo("TEST_REFRESH_TOKEN");
        assertThat(accessMeans.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(accessMeans.getScope()).isEqualTo("AIS");
        assertThat(accessMeans.getExpiresIn()).isEqualTo(601L);
    }

    @Test
    void shouldFetchData() throws IOException, URISyntaxException, TokenInvalidException, ProviderFetchDataException {
        // given
        var testUserId = UUID.randomUUID();
        var account1Fixture = ArgentaFixtures.account1Fixture(clock);
        var account2Fixture = ArgentaFixtures.account2Fixture(clock);
        var account1BookedTransactionIndex0Fixture = ArgentaFixtures.account1BookedTransactionIndex0Fixture();
        var account1BookedTransactionIndex1Fixture = ArgentaFixtures.account1BookedTransactionIndex1Fixture();
        var account1PendingTransactionIndex5Fixture = ArgentaFixtures.account1PendingTransactionIndex5Fixture();

        var request = new UrlFetchDataRequestBuilder()
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .setAuthenticationMeans(ArgentaAuthenticationMeansFixtures.getSampleAuthenticationMeans())
                .setAccessMeans(testUserId, getSerializedAccessMeans(), new Date(), new Date())
                .setRestTemplateManager(restTemplateManager)
                .setSigner(SIGNER)
                .setTransactionsFetchStartTime(TEST_TRANSACTION_FETCH_START_TIME)
                .build();

        // when
        var result = dataProvider.fetchData(request);

        // then
        assertThat(result.getAccounts()).hasSize(2);


        assertThat(result).extracting(DataProviderResponse::getAccounts)
                .asList()
                .element(0)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFields("transactions", "extendedAccount")
                .isEqualTo(account1Fixture);

        assertThat(result.getAccounts().get(0).getExtendedAccount())
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(account1Fixture.getExtendedAccount());

        assertThat(result).extracting(DataProviderResponse::getAccounts)
                .asList()
                .element(1)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringFields("transactions", "extendedAccount")
                .isEqualTo(account2Fixture);

        assertThat(result.getAccounts().get(1).getExtendedAccount())
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(account2Fixture.getExtendedAccount());

        assertThat(result.getAccounts()).element(0)
                .extracting(ProviderAccountDTO::getTransactions)
                .asList()
                .hasSize(6);

        assertThat(result.getAccounts())
                .element(0)
                .extracting(ProviderAccountDTO::getTransactions)
                .asList()
                .element(0)
                .isEqualTo(account1BookedTransactionIndex0Fixture);

        assertThat(result.getAccounts())
                .element(0)
                .extracting(ProviderAccountDTO::getTransactions)
                .asList()
                .element(1)
                .isEqualTo(account1BookedTransactionIndex1Fixture);

        assertThat(result.getAccounts())
                .element(0)
                .extracting(ProviderAccountDTO::getTransactions)
                .asList()
                .element(5)
                .isEqualTo(account1PendingTransactionIndex5Fixture);
    }

    @Test
    void shouldDeleteUserSite() throws IOException, URISyntaxException, TokenInvalidException {
        // given
        var testUserId = UUID.randomUUID();

        var request = new UrlOnUserSiteDeleteRequestBuilder()
                .setAuthenticationMeans(ArgentaAuthenticationMeansFixtures.getSampleAuthenticationMeans())
                .setRestTemplateManager(restTemplateManager)
                .setSigner(SIGNER)
                .setExternalConsentId(TEST_CONSENT_ID)
                .setPsuIpAddress(TEST_PSU_IP_ADDRESS)
                .setAccessMeans(
                        new AccessMeansDTO(
                                testUserId,
                                getSerializedAccessMeans(),
                                new Date(),
                                new Date()
                        )
                ).build();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> dataProvider.onUserSiteDelete(request);

        // then
        assertThatCode(throwingCallable).doesNotThrowAnyException();
    }

    private String getSerializedAccessMeans() {
        return """
                {"accessToken":"TEST_ACCESS_TOKEN","refreshToken":"TEST_REFRESH_TOKEN","scope":"AIS","expiresIn":"600","consentId":"TEST_CONSENT_ID"}""";
    }

    private String getProviderState() {
        return """
                {"consentId":"TEST_CONSENT_ID","proofKeyCodeExchange":{"codeVerifier":"yilZ1INlFf5MqX9Q2OuvrgVI0qp6qzhDdFRqy5wnbPI=","codeChallenge":"eaccdc4457a694b8abba4346116f6077596ecb6929b9b147865fbd1380d565b3","codeChallengeMethod":"S256"}}""";
    }
}