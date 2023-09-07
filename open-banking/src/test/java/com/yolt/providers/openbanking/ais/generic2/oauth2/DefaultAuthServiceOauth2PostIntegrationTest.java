package com.yolt.providers.openbanking.ais.generic2.oauth2;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.configuration.OpenbankingConfiguration;
import com.yolt.providers.openbanking.ais.generic2.GenericApp;
import com.yolt.providers.openbanking.ais.generic2.SignerMock;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeansBuilder;
import com.yolt.providers.openbanking.ais.generic2.claims.producer.DefaultJwtClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.claims.token.DefaultTokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.configuration.GenericTestProperties;
import com.yolt.providers.openbanking.ais.generic2.configuration.auth.GenericSampleTypedAuthenticationMeans;
import com.yolt.providers.openbanking.ais.generic2.configuration.resttemplatemanager.RestTemplateManagerMock;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.oauth2.implementations.DefaultClientSecretPostOauth2Client;
import com.yolt.providers.openbanking.ais.generic2.service.DefaultAuthenticationService;
import com.yolt.providers.openbanking.ais.generic2.signer.ExternalUserRequestTokenSigner;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {GenericApp.class, OpenbankingConfiguration.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(stubs = "classpath:/stubs/generic/oauth2", httpsPort = 0, port = 0)
@ActiveProfiles({"generic2", "generic-post2"})
public class DefaultAuthServiceOauth2PostIntegrationTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String PROVIDER_KEY = "TEST_IMPL_OPENBANKING";
    private static final TokenScope SCOPE = TokenScope.builder().grantScope("openid accounts").authorizationUrlScope("openid accounts").build();
    private static final AuthenticationMeansReference AUTHENTICATION_MEANS_REFERENCE = new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID());
    private final Signer signer = new SignerMock();
    private RestTemplateManagerMock restTemplateManagerMock;
    @Autowired
    private GenericTestProperties properties;
    private Oauth2Client oauth2Client;
    @Autowired
    private HttpClientFactory httpClientFactory;
    @Autowired
    private Clock clock;

    private DefaultAuthenticationService authenticationService;

    private DefaultAuthMeans authenticationMeans;

    @BeforeEach
    public void setup() throws IOException, URISyntaxException {
        oauth2Client = new DefaultClientSecretPostOauth2Client(properties, false);
        authenticationService = new DefaultAuthenticationService(properties.getOAuthAuthorizationUrl(), oauth2Client,
                new ExternalUserRequestTokenSigner(AlgorithmIdentifiers.RSA_PSS_USING_SHA256),
                new DefaultTokenClaimsProducer(new DefaultJwtClaimsProducer(defaultAuthMeans -> null, properties.getAudience())),
                clock);
        authenticationMeans = new DefaultAuthMeansBuilder().createAuthenticationMeans(
                new GenericSampleTypedAuthenticationMeans().getAuthenticationMeans(), PROVIDER_KEY);
        restTemplateManagerMock = new RestTemplateManagerMock(() -> "657345");
    }

    @Test
    public void shouldReturnAuthorizationUrlForGenerateAuthorizationUrlWithCorrectParameters() {
        // given
        String accountRequestId = "1234";
        String secretState = UUID.randomUUID().toString();
        String redirectUrl = "http://yolt.com/identifier";

        // when
        String authorizationUrl = authenticationService.generateAuthorizationUrl(authenticationMeans, accountRequestId,
                secretState, redirectUrl, SCOPE, signer);

        // then
        assertThat(authorizationUrl).isNotEmpty();
    }

    @Test
    public void shouldReturnNewAccessMeansForCreateAccessTokenWithCorrectParameters() throws TokenInvalidException {
        // given
        HttpClient httpClient = httpClientFactory.createHttpClient(restTemplateManagerMock, authenticationMeans, "any");
        String authorizationCode = "gktvoeyJlbmMiOiJBMTI4Q0JDLUhTMjU2IiwiYWxnIjoiUlNBMV81Iiwia2lkIjoicTQtMjAxNy1tMi1CT1MifQ.JvophKQTiXv8tvE66jNaXidcYWw_a8BacizAdMiePt_Dd9zJAFU5-TN0qwVIwbIBWbc3hxmiz6VIyJjLoFVAb14QcJaBVuqAiv6Ci8Q752UA-R1aK-t3K1cT5iMtsGlO_7x2EfJum6ujZyCkeTQdKrdnYqH5r1VCLSLxlXFQedXUQ4xYOQr06b4Twj-APIH1dl6WKmIWTyvoFU6_FqGZVNFc_t8VE2KiUjnJnFyFlsF54077WFKiecSAzE_tOFqp0RN_eAaM8J4ycyBoO-cjJ3bJvBB3sXctoCG-lnSxQtP4c2eu0Qg6NIXpAiFEe562w0JRzW1d1ZFNjmBY4jGRIA.PAnSqNZdL4s539MyX4i-Rg.gepH1P5F_rrG5CCEMMkDQPRyxGcYdc136rVvwZs5sZS9kB9357PLJ7asdf8yeafjIKI-l-FoogsOvVf6dQE2_iVAmrTOoESGdk5szYvGC8_kSYmD8j2Kl9Px7xvjbaki-fW5wyR0F8c9MTRvT7aEx2JVy5RHq8hsMguAmCmTNi2NzyZXHhNoNxKmesYJpE2Bz-2bHBfWH1VakuhTp8751atBvbWvU97CMDbUAQx18QW4gL8pWaVtYfDx_5CfF6DP6Cv4RiK_NngCSV5CrdgcDhMWPZeeY41lVVITclG4-tpMZE3bp9W4NB2LYX_zShAR9OsnbD6qgHtwC_-6PfaPrNIW5PpTJK73IRzLxsU-bflLea4fHI2dtXSdL5msUqpM-kS-_tPBXweXT42AzIBNbIZ4Jj7R6WOhign5gx2Z_c3vj--1Pq2zh2ztZHwQ8s3oh5qUwkW_vrLG4ruL4MUDz_8MwTiTRNXZYRvq-M6fZAzN7B3_ykLHUbpoiGAl1Eli0Yw8N98WrcAfC6BWcwc2d-6hrwen6_QcZw0yX2nEt8bCRQwsbYoEE9PV3m38U0M3PAcqHkazVELJz4Afx_naFVRq6dlafQAuZbeS8kBF1gIhTubdWgQFEyCvIHvh5a_takLkDJimjrbYHsREykcrVdnJ73c_t4v6K5aWj7UOJ6p0w7nRjHBtV0uXlFJP-qfp.LZMdA6nFUbqat01P6uJFUA";
        String redirectUrl = "https://www.yolt.com/callback/3651edaa-d36e-48cb-8cc3-94bb1fbe8f76#code=" + authorizationCode + "&state=secretState";

        // when
        AccessMeans accessToken = authenticationService.createAccessToken(httpClient, authenticationMeans, USER_ID,
                authorizationCode, redirectUrl, SCOPE, signer);

        // then
        assertThat(accessToken.getAccessToken()).isNotEmpty();
    }

    @Test
    public void shouldReturnClientAccessTokenForGetClientAccessTokenWithCorrectParameters() {
        // given
        HttpClient httpClient = httpClientFactory.createHttpClient(restTemplateManagerMock, authenticationMeans, "any");

        // when
        AccessMeans accessToken = authenticationService.getClientAccessToken(httpClient, authenticationMeans,
                AUTHENTICATION_MEANS_REFERENCE, SCOPE, signer);

        // then
        assertThat(accessToken.getAccessToken()).isNotEmpty();
    }

    @Test
    public void shouldCreateNewAccessMeansForRefreshAccessTokenWithCorrectParameters() throws TokenInvalidException {
        // given
        HttpClient httpClient = httpClientFactory.createHttpClient(restTemplateManagerMock, authenticationMeans, "any");
        String refreshToken = "refreshToken";
        String redirectUrl = "http://yolt.com/identifier";

        // when
        AccessMeans accessToken = authenticationService.refreshAccessToken(httpClient, authenticationMeans, USER_ID,
                refreshToken, redirectUrl, SCOPE, signer);

        // then
        assertThat(accessToken.getAccessToken()).isNotEmpty();
    }
}