package com.yolt.providers.cbiglobe.common.service;

import com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import com.yolt.providers.cbiglobe.common.model.TokenResponse;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CbiGlobeAuthorizationServiceTest {

    private static final UUID CLIENT_ID1 = UUID.fromString("9bf63f88-2140-4a6d-a267-a5c40f50c282");
    private static final UUID CLIENT_REDIRECT_URL1 = UUID.fromString("0c385059-3de0-44e5-9da7-0c7729d33e6a");
    private static final UUID CLIENT_ID2 = UUID.fromString("8d162134-eaea-4a34-bf02-2eccf4d48d39");
    private static final UUID CLIENT_REDIRECT_URL2 = UUID.fromString("b8063f37-cecc-4103-9286-23d0cdaf28a9");

    private CbiGlobeAuthorizationService authorizationService;
    private CbiGlobeAuthenticationMeans authenticationMeans;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setup() {
        authenticationMeans = CbiGlobeAuthenticationMeans
                .builder()
                .clientId("fakeclientid")
                .clientSecret("fakeclientsecret")
                .build();

        CbiGlobeBaseProperties properties = new CbiGlobeBaseProperties();
        properties.setTokenUrl("https://localhost:8080/auth/oauth/v2/token");

        authorizationService = new CbiGlobeAuthorizationService(properties, Clock.systemUTC());
    }

    @Test
    void shouldSendRequestForClientAccessTokenOnlyOnceForGetClientAccessTokenCalledTwiceWithCorrectRequestDataForTheSameAuthMeans() {
        // given
        AuthenticationMeansReference reference = new AuthenticationMeansReference(CLIENT_ID1, CLIENT_REDIRECT_URL1);
        ResponseEntity<TokenResponse> response = createTokenResponse();

        when(restTemplate.postForEntity(anyString(), any(), eq(TokenResponse.class))).thenReturn(response);

        // when
        authorizationService.getClientAccessToken(restTemplate, reference, authenticationMeans);
        authorizationService.getClientAccessToken(restTemplate, reference, authenticationMeans);

        // then
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(TokenResponse.class));
    }

    @Test
    void shouldSendRequestForClientAccessTokenTwiceForGetClientAccessTokenCalledTwiceWithCorrectRequestDataForDifferentAuthMeans() {
        // given
        AuthenticationMeansReference reference1 = new AuthenticationMeansReference(CLIENT_ID1, CLIENT_REDIRECT_URL1);
        AuthenticationMeansReference reference2 = new AuthenticationMeansReference(CLIENT_ID2, CLIENT_REDIRECT_URL2);
        ResponseEntity<TokenResponse> response = createTokenResponse();

        when(restTemplate.postForEntity(anyString(), any(), eq(TokenResponse.class))).thenReturn(response);

        // when
        authorizationService.getClientAccessToken(restTemplate, reference1, authenticationMeans);
        authorizationService.getClientAccessToken(restTemplate, reference2, authenticationMeans);

        // then
        verify(restTemplate, times(2)).postForEntity(anyString(), any(), eq(TokenResponse.class));
    }

    private ResponseEntity<TokenResponse> createTokenResponse() {
        return ResponseEntity.ok(TokenResponse
                .builder()
                .accessToken("access-token")
                .tokenType("token-type")
                .expiresIn("9999")
                .scope("accounts")
                .build());
    }
}