package com.yolt.providers.openbanking.ais.monzogroup.ais.V5;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequest;
import com.yolt.providers.common.ais.url.UrlRefreshAccessMeansRequestBuilder;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericBaseDataProvider;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.monzogroup.MonzoApp;
import com.yolt.providers.openbanking.ais.monzogroup.MonzoSampleTypedAuthMeansV2;
import com.yolt.providers.openbanking.ais.monzogroup.MonzoTestUtilV2;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * This test contains all token exception flows occurring in Monzo bank provider.
 * <p>
 * Covered flows:
 * - get access token fail
 * - refresh access token fail
 * <p>
 */
@SpringBootTest(classes = {MonzoApp.class, OpenbankingConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/monzogroup/ob_3.1/ais/refresh-access-means-400/", httpsPort = 0, port = 0)
@ActiveProfiles("monzogroup")
public class MonzoDataProviderV5TokenExceptionTest {
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String TEST_REDIRECT_URL = "https://www.test-url.com/";
    private static AccessMeans INVALID_MONZO_ACCESS_MEANS;

    private RestTemplateManagerMock restTemplateManagerMock;

    @Autowired
    @Qualifier("MonzoDataProviderV5")
    private GenericBaseDataProvider monzoDataProvider;

    @Autowired
    @Qualifier("OpenBanking")
    private ObjectMapper objectMapper;

    private Map<String, BasicAuthenticationMean> authenticationMeans;

    @BeforeAll
    public static void setup() {
        INVALID_MONZO_ACCESS_MEANS = new AccessMeans(
                Instant.now(),
                USER_ID,
                "Az90SAOJklae",
                "invalidatedRefreshToken",
                Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                Date.from(Instant.now()),
                TEST_REDIRECT_URL);
    }

    @BeforeEach
    public void beforeEach() throws IOException, URISyntaxException {
        authenticationMeans = new MonzoSampleTypedAuthMeansV2().getAuthenticationMeans();
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "93bac548-d2de-4546-b106-880a5018460d");
    }

    @Test
    public void shouldThrowTokenInvalidExceptionDuringRefreshToken() {
        // given
        AccessMeansDTO accessMeans = new AccessMeansDTO(USER_ID, MonzoTestUtilV2.getSerializedAccessMeans(INVALID_MONZO_ACCESS_MEANS, objectMapper), new Date(), new Date());
        UrlRefreshAccessMeansRequest urlRefreshAccessMeans = new UrlRefreshAccessMeansRequestBuilder()
                .setAccessMeans(accessMeans)
                .setAuthenticationMeans(authenticationMeans)
                .setRestTemplateManager(restTemplateManagerMock)
                .build();

        // when
        ThrowingCallable fetchDataCallable = () -> monzoDataProvider.refreshAccessMeans(urlRefreshAccessMeans);

        // then
        assertThatThrownBy(fetchDataCallable).isExactlyInstanceOf(TokenInvalidException.class);
    }

    @Test
    public void shouldThrowGetAccessTokenFailedExceptionWhenErrorIsInvalidGrant() {
        UrlCreateAccessMeansRequest urlCreateAccessMeans = new UrlCreateAccessMeansRequestBuilder()
                .setRedirectUrlPostedBackFromSite("http://example.com?error=invalid_grant")
                .build();

        // when
        ThrowingCallable fetchDataCallable = () -> monzoDataProvider.createNewAccessMeans(urlCreateAccessMeans);

        // then
        assertThatThrownBy(fetchDataCallable).isExactlyInstanceOf(GetAccessTokenFailedException.class);
    }
}
