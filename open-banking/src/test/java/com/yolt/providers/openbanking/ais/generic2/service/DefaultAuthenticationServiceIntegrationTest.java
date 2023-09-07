package com.yolt.providers.openbanking.ais.generic2.service;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericApp;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeansBuilder;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.configuration.GenericTestProperties;
import com.yolt.providers.openbanking.ais.generic2.configuration.auth.GenericSampleTypedAuthenticationMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.Oauth2Client;
import com.yolt.providers.openbanking.ais.generic2.oauth2.implementations.DefaultClientSecretBasicOauth2Client;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders.SIGNATURE_HEADER_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {GenericApp.class, OpenbankingConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("generic2")
public class DefaultAuthenticationServiceIntegrationTest {

    private static final String PROVIDER_KEY = "TEST_IMPL_OPENBANKING";
    private static final AuthenticationMeansReference AUTHENTICATION_MEANS_REFERENCE = new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID());
    private static final TokenScope SCOPE = TokenScope.builder().grantScope("accounts").authorizationUrlScope("openid accounts").build();

    @Autowired
    private GenericTestProperties properties;

    @Autowired
    private Clock clock;

    private Oauth2Client oauth2Client;

    @Mock
    private HttpClient httpClient;

    @Mock
    private Signer signer;

    private DefaultAuthenticationService authenticationService;

    private DefaultAuthMeans authenticationMeans;


    @BeforeEach
    public void setup() throws IOException, URISyntaxException {
        oauth2Client = new DefaultClientSecretBasicOauth2Client(properties, false);
        authenticationService = new DefaultAuthenticationService(properties.getOAuthAuthorizationUrl(), oauth2Client,
                new ExternalUserRequestTokenSigner(AlgorithmIdentifiers.RSA_PSS_USING_SHA256),
                new DefaultTokenClaimsProducer(new DefaultJwtClaimsProducer(defaultAuthMeans -> null, properties.getAudience())),
                clock);
        authenticationMeans = new DefaultAuthMeansBuilder().createAuthenticationMeans(
                new GenericSampleTypedAuthenticationMeans().getAuthenticationMeans(), PROVIDER_KEY);
    }


    @Test
    public void shouldCallOAuthTokenUrlEndpointOnceDuring5SecondsConcurrentCallsForExchangeWhenValidRequestData() throws TokenInvalidException, InterruptedException {
        // given
        final String newAccessToken = "newAccessToken";
        final String newRefreshToken = "newRefreshToken";
        String clientCredentialsResponse = String.format("{\n" +
                " \"access_token\": \"%s\",\n" +
                " \"token_type\": \"Bearer\",\n" +
                " \"expires_in\": 3600,\n" +
                " \"refresh_token\": \"%s\"\n" +
                "}", newAccessToken, newRefreshToken);
        when(httpClient.exchange(eq(properties.getOAuthTokenUrl()), eq(HttpMethod.POST), any(HttpEntity.class), anyString(), any(), any(), any()))
                .then(invocationOnMock -> {
                    Thread.sleep(100L);
                    return ResponseEntity.status(200).header(SIGNATURE_HEADER_NAME).body(clientCredentialsResponse);
                });
        int nrOfTasks = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(nrOfTasks);
        CountDownLatch latch = new CountDownLatch(nrOfTasks);

        // when
        for (int i = 0; i < nrOfTasks; i++) {
            long millisWait = 5 * i;
            executorService.submit(() -> {
                try {
                    Thread.sleep(millisWait);
                    authenticationService.refreshClientAccessToken(httpClient, authenticationMeans, AUTHENTICATION_MEANS_REFERENCE, SCOPE, signer);
                } catch (InterruptedException e) {
                    Assertions.fail();
                } finally {
                    latch.countDown();
                }
            });
        }
        boolean await = latch.await(5, TimeUnit.SECONDS);

        // then
        assertThat(await)
                .withFailMessage("should not take more than 5 seconds")
                .isTrue();

        // should just be 1
        verify(httpClient, times(1)).exchange(eq(properties.getOAuthTokenUrl()), eq(HttpMethod.POST),
                any(HttpEntity.class), anyString(), any(), any(), any());

    }
}