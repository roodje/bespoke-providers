package com.yolt.providers.stet.generic.service.authorization;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.dto.TestTokenResponseDTO;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.authorization.refresh.RefreshTokenSupportedStrategy;
import com.yolt.providers.stet.generic.service.authorization.request.AccessMeansRequest;
import com.yolt.providers.stet.generic.service.authorization.request.RefreshTokenRequest;
import com.yolt.providers.stet.generic.service.authorization.request.TokenRequest;
import com.yolt.providers.stet.generic.service.authorization.rest.AuthorizationRestClient;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenSupportedStrategyTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2020-01-15T12:00:00.00Z"), ZoneId.systemDefault());

    @Mock
    private HttpClient httpClient;

    @Mock
    private AuthorizationRestClient authorizationRestClient;

    @Mock
    private ProviderStateMapper providerStateMapper;

    @Mock
    private Signer signer;

    @Captor
    private ArgumentCaptor<RefreshTokenRequest> refreshTokenRequestArgumentCaptor;

    @Captor
    private ArgumentCaptor<DataProviderState> providerStateArgumentCaptor;

    @BeforeEach
    void beforeEach() {
        refreshTokenSupportedStrategy = new RefreshTokenSupportedStrategy(
                providerStateMapper,
                Scope.AISP,
                authorizationRestClient,
                new DateTimeSupplier(CLOCK));
    }

    private RefreshTokenSupportedStrategy refreshTokenSupportedStrategy;

    @Test
    void shouldReturnAccessMeansForRefreshAccessMeansWhenCorrectData() throws TokenInvalidException {
        // given
        UUID userId = UUID.randomUUID();
        Region region = createRegion();
        DataProviderState providerState = DataProviderState.authorizedProviderState(region, "oldAccessToken", "oldRefreshToken");
        DefaultAuthenticationMeans authMeans = createDefaultAuthenticationMeans();
        AccessMeansRequest accessMeansRequest = new AccessMeansRequest(
                authMeans,
                new AccessMeansDTO(userId, "providerState", Date.from(Instant.now(CLOCK)), Date.from(Instant.now(CLOCK).plusSeconds(1))),
                providerState,
                signer);
        TokenResponseDTO tokenResponseDTO = createTokenResponseDTO();

        when(authorizationRestClient.refreshAccessToken(any(HttpClient.class), any(RefreshTokenRequest.class), any(DefaultAuthenticationMeans.class), any()))
                .thenReturn(tokenResponseDTO);
        when(providerStateMapper.mapToJson(any(DataProviderState.class)))
                .thenReturn("newProviderState");

        // when
        AccessMeansDTO accessMeansDTO = refreshTokenSupportedStrategy.refreshAccessMeans(httpClient, accessMeansRequest);

        // then
        assertThat(accessMeansDTO.getUserId()).isEqualTo(userId);
        assertThat(accessMeansDTO.getAccessMeans()).isEqualTo("newProviderState");
        assertThat(accessMeansDTO.getUpdated()).isEqualTo(Date.from(Instant.now(CLOCK)));
        assertThat(accessMeansDTO.getExpireTime()).isEqualTo(Date.from(Instant.now(CLOCK).plusSeconds(1)));
        verify(authorizationRestClient).refreshAccessToken(eq(httpClient), refreshTokenRequestArgumentCaptor.capture(), eq(authMeans), eq(TokenResponseDTO.class));
        RefreshTokenRequest capturedRefreshTokenRequest = refreshTokenRequestArgumentCaptor.getValue();
        assertThat(capturedRefreshTokenRequest)
                .extracting(TokenRequest::getTokenUrl, RefreshTokenRequest::getRefreshToken, it -> it.getAuthMeans().getClientId(), it -> it.getAuthMeans().getClientSecret())
                .contains("http://localhost/region1/token", "oldRefreshToken", "fakeClient", null);
        verify(providerStateMapper).mapToJson(providerStateArgumentCaptor.capture());
        DataProviderState capturedProviderState = providerStateArgumentCaptor.getValue();
        assertThat(capturedProviderState).extracting(DataProviderState::getRegion, DataProviderState::getAccessToken, DataProviderState::getRefreshToken)
                .contains(region, "newAccessToken", "newRefreshToken");
    }

    private TokenResponseDTO createTokenResponseDTO() {
        return TestTokenResponseDTO.builder()
                .accessToken("newAccessToken")
                .refreshToken("newRefreshToken")
                .expiresIn(1)
                .build();
    }

    private DefaultAuthenticationMeans createDefaultAuthenticationMeans() {
        return DefaultAuthenticationMeans.builder()
                .clientId("fakeClient")
                .build();
    }

    private Region createRegion() {
        Region region = new Region();
        region.setTokenUrl("http://localhost/region1/token");
        region.setName("Region 1");
        region.setCode("REGION1");
        region.setBaseUrl("http://localhost/region1");
        region.setAuthUrl("http://localhost/region1/authorize");
        return region;
    }
}
