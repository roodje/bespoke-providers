package com.yolt.providers.openbanking.ais.generic2.service;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeansBuilder;
import com.yolt.providers.openbanking.ais.generic2.claims.token.TokenClaimsProducer;
import com.yolt.providers.openbanking.ais.generic2.configuration.auth.GenericSampleTypedAuthenticationMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.AccessTokenResponseDTO;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.oauth2.Oauth2Client;
import com.yolt.providers.openbanking.ais.generic2.signer.UserRequestTokenSigner;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoCacheAuthenticationServiceTest {

    private static final String PROVIDER_KEY = "TEST_IMPL_OPENBANKING";
    private static final AuthenticationMeansReference AUTHENTICATION_MEANS_REFERENCE = new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID());
    private static final TokenScope SCOPE = TokenScope.builder().grantScope("accounts").authorizationUrlScope("openid accounts").build();
    private static final Clock CLOCK = Clock.systemUTC();

    private NoCacheAuthenticationService subject;

    @Mock
    private Oauth2Client oauth2Client;

    @Mock
    private UserRequestTokenSigner userRequestTokenSigner;

    @Mock
    private TokenClaimsProducer tokenClaimsProducer;

    @Mock
    private HttpClient httpClient;

    @Mock
    private Signer signer;

    private DefaultAuthMeans authenticationMeans;

    @BeforeEach
    void setup() throws IOException, URISyntaxException {
        authenticationMeans = new DefaultAuthMeansBuilder().createAuthenticationMeans(
                new GenericSampleTypedAuthenticationMeans().getAuthenticationMeans(), PROVIDER_KEY);

        subject = new NoCacheAuthenticationService(
                "https://localhost/oauth-token",
                oauth2Client,
                userRequestTokenSigner,
                tokenClaimsProducer,
                CLOCK
        );
    }

    @Test
    void shouldAlwaysGetNewAccessToken() throws TokenInvalidException {
        //Given
        var accessTokenResponse = new AccessTokenResponseDTO("accessToken", "refreshToken", 1000, "tokenType", "idToken", "scope");
        when(oauth2Client.createClientCredentials(httpClient, authenticationMeans, SCOPE, signer)).thenReturn(accessTokenResponse);

        //When
        subject.getClientAccessToken(httpClient, authenticationMeans, AUTHENTICATION_MEANS_REFERENCE, SCOPE, signer);
        subject.getClientAccessToken(httpClient, authenticationMeans, AUTHENTICATION_MEANS_REFERENCE, SCOPE, signer);

        //Then
        verify(oauth2Client, times(2)).createClientCredentials(httpClient, authenticationMeans, SCOPE, signer);
    }
}