package com.yolt.providers.axabanque.common.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.axabanque.common.auth.http.client.DefaultAuthorizationHttpClientV2;
import com.yolt.providers.axabanque.common.auth.http.clientproducer.DefaultHttpClientProducer;
import com.yolt.providers.axabanque.common.auth.mapper.access.AccessMeansMapper;
import com.yolt.providers.axabanque.common.auth.mapper.access.DefaultAccessMeansMapper;
import com.yolt.providers.axabanque.common.auth.mapper.access.DefaultAccessTokenMapper;
import com.yolt.providers.axabanque.common.auth.service.AuthenticationService;
import com.yolt.providers.axabanque.common.auth.service.DefaultAuthenticationService;
import com.yolt.providers.axabanque.common.config.GroupProperties;
import com.yolt.providers.axabanque.common.fixtures.AccessMeansFixture;
import com.yolt.providers.axabanque.common.fixtures.ProviderStateFixture;
import com.yolt.providers.axabanque.common.fixtures.TokenDtoFixture;
import com.yolt.providers.axabanque.common.model.external.AuthorizationResponse;
import com.yolt.providers.axabanque.common.model.external.ConsentResponse;
import com.yolt.providers.axabanque.common.model.external.Token;
import com.yolt.providers.axabanque.common.model.internal.AccessToken;
import com.yolt.providers.axabanque.common.model.internal.GroupAccessMeans;
import com.yolt.providers.axabanque.common.model.internal.GroupProviderState;
import com.yolt.providers.axabanque.common.pkce.PKCE;
import com.yolt.providers.axabanque.common.traceid.TraceIdProducer;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.securityutils.oauth2.OAuth2ProofKeyCodeExchange;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.cert.X509Certificate;
import java.time.Clock;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultAuthenticationServiceTest {
    private Clock clock;
    private AuthenticationService authenticationService;
    private DefaultAuthorizationHttpClientV2 httpClient;
    private PKCE pkce;
    private RestTemplateManager restTemplateManager;
    private AccessMeansMapper accessMeanMapper;
    private TraceIdProducer traceIdProducer;
    private Function<Token, AccessToken> accessTokenMapper;

    @BeforeEach
    public void setup() {
        clock = Clock.systemUTC();
        restTemplateManager = mock(RestTemplateManager.class);
        httpClient = mock(DefaultAuthorizationHttpClientV2.class);
        DefaultHttpClientProducer restTemplateProducer = mock(DefaultHttpClientProducer.class);
        when(restTemplateProducer.getAuthenticationHttpClient(any(), any(), eq(restTemplateManager))).thenReturn(httpClient);
        GroupProperties properties = new GroupProperties();
        properties.setBaseUrl("http://urlToAxa.com");
        properties.setAuthorizationBaseUrl("http://urlToAxa.com/public");
        pkce = mock(PKCE.class);
        traceIdProducer = mock(TraceIdProducer.class);
        accessMeanMapper = mock(DefaultAccessMeansMapper.class);
        accessTokenMapper = mock(DefaultAccessTokenMapper.class);
        authenticationService = new DefaultAuthenticationService(
                clock,
                pkce,
                restTemplateProducer,
                properties,
                new ObjectMapper(),
                accessMeanMapper,
                accessTokenMapper,
                traceIdProducer);
    }

    @Test
    void shouldReturnLoginInfo() throws JsonProcessingException, TokenInvalidException {
        //given
        when(traceIdProducer.get()).thenReturn("xRequestId");
        GroupAuthenticationMeans authenticationMeans = new GroupAuthenticationMeans(UUID.randomUUID(), mock(X509Certificate.class), "clientId");
        when(pkce.createRandomS256()).thenReturn(new OAuth2ProofKeyCodeExchange("codeVerifier", "code", "dummyCodeChallengeMethod"));
        ConsentResponse consentResponseDTO = mock(ConsentResponse.class);
        when(consentResponseDTO.getConsentId()).thenReturn("consentId");
        when(httpClient.initiateConsent(eq("redirectUri"), eq("psuIp"), any(), any()))
                .thenReturn(consentResponseDTO);
        AuthorizationResponse authorizationResponseDTO = mock(AuthorizationResponse.class);
        when(authorizationResponseDTO.getAuthorisationIds()).thenReturn(Collections.singletonList("authorizationId"));
        when(httpClient.initiateAuthorizationResource(consentResponseDTO, "xRequestId"))
                .thenReturn(authorizationResponseDTO);

        GroupProviderState expectedProviderState = ProviderStateFixture.createProviderState("codeVerifier", "code", "consentId", "xRequestId", 1L);
        RedirectStep expectedRedirectStep = new RedirectStep("http://urlToAxa.com/public/authorize/authorizationId?" +
                "response_type=code&client_id=clientId&scope=AIS:consentId&state=state&redirect_uri=redirectUri&code_challenge=code&code_challenge_method=dummyCodeChallengeMethod",
                consentResponseDTO.getConsentId(), new ObjectMapper().writeValueAsString(expectedProviderState));
        //when
        RedirectStep loginInfo = authenticationService.getLoginInfo(authenticationMeans,
                "state", "redirectUri", "psuIp", restTemplateManager);
        //then
        assertThat(loginInfo)
                .usingRecursiveComparison()
                .ignoringFields("providerState")
                .isEqualTo(expectedRedirectStep);
        GroupProviderState providerState = new ObjectMapper().readValue(loginInfo.getProviderState(), GroupProviderState.class);

        assertThat(providerState)
                .usingRecursiveComparison()
                .ignoringFields("consentGeneratedAt")
                .isEqualTo(expectedProviderState);
    }

    @Test
    void shouldCreateAccessMeans() throws JsonProcessingException, TokenInvalidException {
        //given
        String postedBackRedirectUri = "http://returnedRedirectUriWithCode?code=code123";
        UUID transportKeyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GroupAuthenticationMeans authMeans = new GroupAuthenticationMeans(transportKeyId, mock(X509Certificate.class), "clientId");
        GroupProviderState providerState = ProviderStateFixture.createProviderState("codeVerifier", "code123", "consentId", "xRequestId", 111L);
        Token tokenDto = TokenDtoFixture.createTokenDtoMock("tokenType", "refreshToken", "scope", 777L, "accessToken");
        when(httpClient.createToken("clientId", "redirectUri", "code123", "codeVerifier"))
                .thenReturn(tokenDto);
        AccessToken accessToken = TokenDtoFixture.createAccessToken(777L, "refreshToken", "scope", "tokenType", "token");
        when(accessTokenMapper.apply(tokenDto)).thenReturn(accessToken);
        when(accessMeanMapper.mapToAccessMeans("redirectUri", providerState, accessToken))
                .thenReturn(new GroupAccessMeans("b", new GroupProviderState("c", "d", "e", "f", 222L),
                        new AccessToken(123L, "g", "h", "i", "j")));
        String serializedProviderState = new ObjectMapper().writeValueAsString(providerState);
        //when
        AccessMeansDTO accessMeans = authenticationService.createAccessMeans(authMeans, serializedProviderState, userId, "redirectUri", postedBackRedirectUri, restTemplateManager);
        //then
        assertThat(accessMeans.getUserId()).isEqualTo(userId);
        assertThat(accessMeans.getExpireTime()).isEqualTo(new Date(777L));
        assertThat(accessMeans.getAccessMeans()).isEqualTo(
                "{\"baseRedirectUri\":\"b\"," +
                        "\"providerState\":{\"codeVerifier\":\"c\",\"code\":\"d\",\"consentId\":\"e\",\"traceId\":\"f\",\"consentGeneratedAt\":222}," +
                        "\"accessToken\":{\"expiresIn\":123,\"refreshToken\":\"g\",\"scope\":\"h\",\"tokenType\":\"i\",\"token\":\"j\"}}");
    }

    @Test
    void shouldRefreshToken() throws TokenInvalidException {
        //given
        UUID transportKeyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GroupAuthenticationMeans authMeans = new GroupAuthenticationMeans(transportKeyId, mock(X509Certificate.class), "clientId");

        GroupAccessMeans accessMeans = AccessMeansFixture.createAccessMeans("redirectUri", "codeVerifier", "code", "consentId",
                "xRequestId", 1L, "refreshToken", "scope", "tokenType", "accessToken");

        Token tokenDto = TokenDtoFixture.createTokenDtoMock("tokenType", "newRefreshToken", "scope", 333L, "accessToken");
        when(httpClient.refreshToken("clientId", "redirectUri", "code", "codeVerifier", "refreshToken"))
                .thenReturn(tokenDto);
        AccessToken accessToken = TokenDtoFixture.createAccessToken(333L, null, "scope", "tokenType", "token");
        when(accessTokenMapper.apply(tokenDto)).thenReturn(accessToken);

        //when
        AccessMeansDTO refreshedAccessMeans = authenticationService.refreshAccessMeans(authMeans, accessMeans, userId, restTemplateManager);
        //then
        assertThat(refreshedAccessMeans.getUserId()).isEqualTo(userId);
        assertThat(refreshedAccessMeans.getExpireTime()).isEqualTo(new Date(333L));

        assertThat(refreshedAccessMeans.getAccessMeans()).isEqualTo(
                "{\"baseRedirectUri\":\"redirectUri\"," +
                        "\"providerState\":{\"codeVerifier\":\"codeVerifier\",\"code\":\"code\",\"consentId\":\"consentId\",\"traceId\":\"xRequestId\",\"consentGeneratedAt\":-618211476000}," +
                        "\"accessToken\":{\"expiresIn\":333,\"refreshToken\":\"refreshToken\",\"scope\":\"scope\",\"tokenType\":\"tokenType\",\"token\":\"token\"}}"
        );
    }

    @Test
    void shouldNotThrowAnyExceptionWhenDeleteConsent() {
        //given
        UUID transportKeyId = UUID.randomUUID();
        GroupAuthenticationMeans authMeans = new GroupAuthenticationMeans(transportKeyId, mock(X509Certificate.class), "clientId");

        GroupAccessMeans accessMeans = AccessMeansFixture.createAccessMeans("redirectUri", "codeVerifier", "code", "consentId",
                "xRequestId", 1L, "refreshToken", "scope", "tokenType", "accessToken");

        //when
        ThrowableAssert.ThrowingCallable handleMethod = () -> authenticationService.deleteConsent(authMeans, accessMeans, restTemplateManager);

        //then
        assertThatCode(handleMethod).doesNotThrowAnyException();
    }
}
