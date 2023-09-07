package com.yolt.providers.stet.generic.service.authorization;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.dynamic.AccessMeansOrStepDTO;
import com.yolt.providers.common.domain.dynamic.step.RedirectStep;
import com.yolt.providers.common.domain.dynamic.step.Step;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.AuthorizationRedirect;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.dto.TestTokenResponseDTO;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.authorization.refresh.RefreshTokenStrategy;
import com.yolt.providers.stet.generic.service.authorization.request.AccessMeansOrStepRequest;
import com.yolt.providers.stet.generic.service.authorization.request.AccessMeansRequest;
import com.yolt.providers.stet.generic.service.authorization.request.AccessTokenRequest;
import com.yolt.providers.stet.generic.service.authorization.request.StepRequest;
import com.yolt.providers.stet.generic.service.authorization.rest.AuthorizationRestClient;
import com.yolt.providers.stet.generic.service.authorization.tool.AuthorizationCodeExtractor;
import com.yolt.providers.stet.generic.service.authorization.tool.AuthorizationRedirectUrlSupplier;
import nl.ing.lovebird.providershared.AccessMeansDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SingleRegionAuthorizationServiceTest {

    private SingleRegionAuthorizationService authorizationService;

    @Mock
    private RefreshTokenStrategy refreshTokenStrategy;

    @Mock
    private DefaultProperties properties;

    @Mock
    private AuthorizationRedirectUrlSupplier authorizationRedirectUrlSupplier;

    @Mock
    private ProviderStateMapper providerStateMapper;

    @Mock
    private AuthorizationCodeExtractor authorizationCodeExtractor;

    @Mock
    private AuthorizationRestClient authorizationRestClient;

    @Mock
    private HttpClient httpClient;

    @Mock
    private Signer signer;

    @Captor
    private ArgumentCaptor<DataProviderState> providerStateArgumentCaptor;

    @Captor
    private ArgumentCaptor<AccessTokenRequest> accessTokenRequestDTOArgumentCaptor;

    private final Clock clock = Clock.fixed(Instant.parse("2020-01-15T12:00:00.00Z"), ZoneId.systemDefault());

    @BeforeEach
    void beforeEach() {
        authorizationService = new SingleRegionAuthorizationService(
                refreshTokenStrategy,
                authorizationRestClient,
                providerStateMapper,
                Scope.AISP,
                properties,
                authorizationCodeExtractor,
                authorizationRedirectUrlSupplier,
                new DateTimeSupplier(clock));
    }

    @Test
    void shouldReturnRedirectStepForGetStepWhenCorrectData() {
        // given
        Region region = new Region();
        region.setTokenUrl("http://localhost/token");
        region.setName("Region 1");
        region.setCode("REGION1");
        region.setBaseUrl("http://localhost");
        region.setAuthUrl("http://localhost/authorize");
        StepRequest stepRequest = StepRequest.baseStepRequest(
                createDefaultAuthenticationMeans(),
                "http://localhost/redirect",
                "fakeState"
        );
        when(properties.getRegions())
                .thenReturn(Collections.singletonList(region));
        when(authorizationRedirectUrlSupplier.createAuthorizationRedirectUrl(anyString(), any(Scope.class), any(StepRequest.class)))
                .thenReturn(AuthorizationRedirect.create("someAuthorizationRedirectUrl"));
        when(providerStateMapper.mapToJson(any(DataProviderState.class)))
                .thenReturn("providerState");

        // when
        Step result = authorizationService.getStep(stepRequest);

        // then
        assertThat(result).isInstanceOf(RedirectStep.class);
        RedirectStep redirectStep = (RedirectStep) result;
        assertThat(redirectStep.getRedirectUrl()).isEqualTo("someAuthorizationRedirectUrl");
        assertThat(redirectStep.getExternalConsentId()).isNull();
        assertThat(redirectStep.getProviderState()).isEqualTo("providerState");

        verify(properties).getRegions();
        verify(authorizationRedirectUrlSupplier).createAuthorizationRedirectUrl("http://localhost/authorize", Scope.AISP, stepRequest);
        verify(providerStateMapper).mapToJson(providerStateArgumentCaptor.capture());
        DataProviderState capturedProviderState = providerStateArgumentCaptor.getValue();
        assertThat(capturedProviderState.getRegion()).isEqualTo(region);
        assertThat(capturedProviderState.getCodeVerifier()).isNull();
        assertThat(capturedProviderState.getAccessToken()).isNull();
        assertThat(capturedProviderState.getRefreshToken()).isNull();
    }

    @Test
    void shouldReturnWrapperWithAccessMeansForCreateAccessMeansOrGetStepWhenCorrectDataProvided() throws TokenInvalidException {
        // given
        UUID userId = UUID.randomUUID();
        AccessMeansOrStepRequest accessMeansOrStepRequest = new AccessMeansOrStepRequest(
                "fakeProviderState",
                createDefaultAuthenticationMeans(),
                "someRedirectUrlPostedBack",
                "someBaseClientRedirectUrl",
                userId,
                "fakeState",
                null,
                signer
        );
        Region region = new Region();
        region.setTokenUrl("http://localhost/token");
        region.setName("Region 1");
        region.setCode("REGION1");
        region.setBaseUrl("http://localhost");
        region.setAuthUrl("http://localhost/authorize");
        DataProviderState preAuthorizedProviderState = DataProviderState.preAuthorizedProviderState(region, null);
        when(providerStateMapper.mapToDataProviderState(anyString()))
                .thenReturn(preAuthorizedProviderState);
        when(authorizationCodeExtractor.extractAuthorizationCode(anyString()))
                .thenReturn("authCode");
        TokenResponseDTO tokenResponseDTO = createTokenResponseDTO();
        when(authorizationRestClient.getAccessToken(any(HttpClient.class), any(AccessTokenRequest.class), any(DefaultAuthenticationMeans.class), any()))
                .thenReturn(tokenResponseDTO);
        when(providerStateMapper.mapToJson(any(DataProviderState.class)))
                .thenReturn("providerState");

        // when
        AccessMeansOrStepDTO result = authorizationService.createAccessMeansOrGetStep(httpClient, accessMeansOrStepRequest);

        // then
        assertThat(result.getStep()).isNull();
        assertThat(result.getAccessMeans())
                .extracting(AccessMeansDTO::getUserId, AccessMeansDTO::getAccessMeans, AccessMeansDTO::getUpdated, AccessMeansDTO::getExpireTime)
                .contains(userId, "providerState", Date.from(Instant.now(clock)), Date.from(Instant.now(clock).plusSeconds(1)));
        verify(providerStateMapper).mapToDataProviderState("fakeProviderState");
        verify(authorizationCodeExtractor).extractAuthorizationCode("someRedirectUrlPostedBack");
        verify(authorizationRestClient).getAccessToken(any(HttpClient.class), accessTokenRequestDTOArgumentCaptor.capture(), any(DefaultAuthenticationMeans.class), eq(TokenResponseDTO.class));
        AccessTokenRequest capturedAccessTokenRequestDTO = accessTokenRequestDTOArgumentCaptor.getValue();
        assertThat(capturedAccessTokenRequestDTO)
                .extracting(AccessTokenRequest::getTokenUrl, AccessTokenRequest::getAuthorizationCode, it -> it.getAuthMeans().getClientId(), AccessTokenRequest::getRedirectUrl, AccessTokenRequest::getProviderState, AccessTokenRequest::getAccessTokenScope)
                .contains("http://localhost/token", "authCode", "fakeClient", "someBaseClientRedirectUrl", preAuthorizedProviderState, Scope.AISP);
        verify(providerStateMapper).mapToJson(providerStateArgumentCaptor.capture());
        DataProviderState capturedProviderState = providerStateArgumentCaptor.getValue();
        assertThat(capturedProviderState).extracting(DataProviderState::getRegion, DataProviderState::getAccessToken, DataProviderState::getRefreshToken)
                .contains(region, "accessToken", "refreshToken");
    }

    @Test
    void shouldReturnAccessMeansDTOForRefreshAccessMeansWhenCorrectDataProvided() throws TokenInvalidException {
        // given
        UUID userId = UUID.randomUUID();
        AccessMeansRequest accessMeansRequest = new AccessMeansRequest(
                createDefaultAuthenticationMeans(),
                new AccessMeansDTO(userId, "providerState", Date.from(Instant.now(clock)), Date.from(Instant.now(clock).plusSeconds(1))),
                DataProviderState.emptyState(),
                signer);
        AccessMeansDTO expectedAccessMeansDTO = new AccessMeansDTO(userId, "updatedProviderState", Date.from(Instant.now(clock)), Date.from(Instant.now(clock).plusSeconds(1)));
        when(refreshTokenStrategy.refreshAccessMeans(any(HttpClient.class), any(AccessMeansRequest.class)))
                .thenReturn(expectedAccessMeansDTO);

        // when
        AccessMeansDTO result = authorizationService.refreshAccessMeans(httpClient, accessMeansRequest);

        // then
        assertThat(result).extracting(AccessMeansDTO::getUserId, AccessMeansDTO::getAccessMeans, AccessMeansDTO::getUpdated, AccessMeansDTO::getExpireTime)
                .contains(userId, "updatedProviderState", Date.from(Instant.now(clock)), Date.from(Instant.now(clock).plusSeconds(1)));
        verify(refreshTokenStrategy).refreshAccessMeans(httpClient, accessMeansRequest);
    }

    private TokenResponseDTO createTokenResponseDTO() {
        return TestTokenResponseDTO.builder()
                .accessToken("accessToken")
                .refreshToken("refreshToken")
                .expiresIn(1)
                .build();
    }

    private DefaultAuthenticationMeans createDefaultAuthenticationMeans() {
        return DefaultAuthenticationMeans.builder()
                .clientId("fakeClient")
                .build();
    }
}
