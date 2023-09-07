package com.yolt.providers.argentagroup.argenta;

import com.yolt.providers.argentagroup.ArgentaGroupTestApp;
import com.yolt.providers.common.ais.url.UrlCreateAccessMeansRequestBuilder;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.GetAccessTokenFailedException;
import com.yolt.providers.common.providerinterface.UrlDataProvider;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


@ActiveProfiles("argenta")
@SpringBootTest(classes = {ArgentaGroupTestApp.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = {"classpath:/stubs/argenta/ais-1.5/create-access-means-invalid-authorization-code"}, httpsPort = 0, port = 0)
class ArgentaGroupDataProviderCreateAccessMeansInvalidAuthorizationCodeIntegrationTest {

    private static final String TEST_REDIRECT_URL = "https://www.yolt.com/callback";
    private static final String TEST_STATE = UUID.randomUUID().toString();

    @Autowired
    private RestTemplateManager restTemplateManager;

    @Autowired
    @Qualifier("argentaDataProviderV1")
    private UrlDataProvider dataProvider;


    @Test
    void shouldThrowTokenInvalidExceptionForResponseWithInvalidAuthorizationCodeMessage() throws IOException, URISyntaxException {
        // given
        var testUserId = UUID.randomUUID();
        var redirectUrlPostedBackFromSite = "https://www.yolt.com/callback?code=invalid_code";

        var request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(ArgentaAuthenticationMeansFixtures.getSampleAuthenticationMeans())
                .setBaseClientRedirectUrl(TEST_REDIRECT_URL)
                .setRestTemplateManager(restTemplateManager)
                .setState(TEST_STATE)
                .setUserId(testUserId)
                .setRedirectUrlPostedBackFromSite(redirectUrlPostedBackFromSite)
                .setProviderState(
                        """
                                {"proofKeyCodeExchange":{"codeVerifier":"yilZ1INlFf5MqX9Q2OuvrgVI0qp6qzhDdFRqy5wnbPI=","codeChallenge":"eaccdc4457a694b8abba4346116f6077596ecb6929b9b147865fbd1380d565b3","codeChallengeMethod":"S256"}}"""
                )
                .build();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> dataProvider.createNewAccessMeans(request);

        // then
        assertThatThrownBy(throwingCallable).isExactlyInstanceOf(GetAccessTokenFailedException.class)
                .hasMessage("Invalid authorization code: HTTP 400");
    }

    @Test
    void shouldThrowGetAccessTokenFailedExceptionForRedirectUrlWithErrorQueryParameter() throws IOException, URISyntaxException {
        // given
        var testUserId = UUID.randomUUID();
        var redirectUrlPostedBackFromSite = "https://www.yolt.com/callback?error=invalid_request";

        var request = new UrlCreateAccessMeansRequestBuilder()
                .setAuthenticationMeans(ArgentaAuthenticationMeansFixtures.getSampleAuthenticationMeans())
                .setBaseClientRedirectUrl(TEST_REDIRECT_URL)
                .setRestTemplateManager(restTemplateManager)
                .setRedirectUrlPostedBackFromSite(redirectUrlPostedBackFromSite)
                .setProviderState(
                        """
                                {"proofKeyCodeExchange":{"codeVerifier":"yilZ1INlFf5MqX9Q2OuvrgVI0qp6qzhDdFRqy5wnbPI=","codeChallenge":"eaccdc4457a694b8abba4346116f6077596ecb6929b9b147865fbd1380d565b3","codeChallengeMethod":"S256"}}"""
                )
                .build();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> dataProvider.createNewAccessMeans(request);

        // then
        assertThatThrownBy(throwingCallable).isExactlyInstanceOf(GetAccessTokenFailedException.class)
                .hasMessage("Got error in callback URL. Login failed. Redirect URL: https://www.yolt.com/callback?error=invalid_request");
    }


}