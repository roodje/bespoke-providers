package com.yolt.providers.stet.generic.service.authorization.rest;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.dto.TestTokenResponseDTO;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.http.error.ExecutionSupplier;
import com.yolt.providers.stet.generic.http.error.HttpErrorHandler;
import com.yolt.providers.stet.generic.mapper.token.TokenRequestMapper;
import com.yolt.providers.stet.generic.service.authorization.request.AccessTokenRequest;
import com.yolt.providers.stet.generic.service.authorization.request.RefreshTokenRequest;
import com.yolt.providers.stet.generic.service.authorization.rest.header.AuthorizationHttpHeadersFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.GET_ACCESS_TOKEN;
import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.REFRESH_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.POST;

@ExtendWith(MockitoExtension.class)
class DefaultAuthorizationRestClientTest {

    private static final String TOKEN_URL = "https://example.com/token";

    @Mock
    private HttpErrorHandler errorHandler;

    @Mock
    private AuthorizationHttpHeadersFactory headersFactory;

    @Mock
    private TokenRequestMapper tokenRequestMapper;

    @Mock
    private HttpClient httpClient;

    @Mock
    private Signer signer;

    @Captor
    private ArgumentCaptor<ExecutionSupplier<?>> executionSupplierArgumentCaptor;

    @Captor
    private ArgumentCaptor<ExecutionInfo> executionInfoArgumentCaptor;

    @InjectMocks
    private DefaultAuthorizationRestClient restClient;

    @Test
    void shouldReturnProperResultForGetAccessTokenWithCorrectData() throws TokenInvalidException {
        // given
        DefaultAuthenticationMeans authMeans = createDefaultAuthenticationMeans();
        AccessTokenRequest accessTokenRequest = createAccessTokenRequest(authMeans);
        TokenResponseDTO expectedTokenResponseDTO = createTokenResponseDTO();
        HttpHeaders expectedHeaders = new HttpHeaders();
        MultiValueMap<String, String> expectedTokenRequestBody = createTokenRequestBody();
        ExecutionInfo expectedExecutionInfo = new ExecutionInfo(TOKEN_URL, POST, expectedHeaders, GET_ACCESS_TOKEN);

        when(headersFactory.createAccessTokenHeaders(any(DefaultAuthenticationMeans.class), any(), any(AccessTokenRequest.class)))
                .thenReturn(expectedHeaders);
        when(tokenRequestMapper.mapAccessTokenRequest(any(AccessTokenRequest.class)))
                .thenReturn(expectedTokenRequestBody);
        when(errorHandler.executeAndHandle(ArgumentMatchers.<ExecutionSupplier<TokenResponseDTO>>any(), any(ExecutionInfo.class)))
                .thenReturn(expectedTokenResponseDTO);

        // when
        TokenResponseDTO tokenResponseDTO = restClient.getAccessToken(httpClient, accessTokenRequest, authMeans, TokenResponseDTO.class);

        // then
        assertThat(tokenResponseDTO).isEqualTo(expectedTokenResponseDTO);
        verify(errorHandler).executeAndHandle(executionSupplierArgumentCaptor.capture(), executionInfoArgumentCaptor.capture());
        verify(tokenRequestMapper).mapAccessTokenRequest(accessTokenRequest);
        verify(headersFactory).createAccessTokenHeaders(authMeans, expectedTokenRequestBody, accessTokenRequest);
        assertThat(executionInfoArgumentCaptor.getValue()).isEqualToComparingFieldByField(expectedExecutionInfo);
    }

    @Test
    void shouldReturnProperResultForRefreshAccessTokenWithCorrectData() throws TokenInvalidException {
        // given
        DefaultAuthenticationMeans authMeans = createDefaultAuthenticationMeans();
        RefreshTokenRequest refreshTokenRequest = createRefreshTokenRequest(authMeans);
        TokenResponseDTO expectedTokenResponseDTO = createTokenResponseDTO();
        HttpHeaders expectedHeaders = new HttpHeaders();
        MultiValueMap<String, String> expectedTokenRequestBody = createTokenRequestBody();
        ExecutionInfo expectedExecutionInfo = new ExecutionInfo(TOKEN_URL, POST, expectedHeaders, REFRESH_TOKEN);

        when(headersFactory.createAccessTokenHeaders(any(DefaultAuthenticationMeans.class), any(), any(RefreshTokenRequest.class)))
                .thenReturn(expectedHeaders);
        when(tokenRequestMapper.mapRefreshTokenRequest(any(RefreshTokenRequest.class)))
                .thenReturn(expectedTokenRequestBody);
        when(errorHandler.executeAndHandle(ArgumentMatchers.<ExecutionSupplier<TokenResponseDTO>>any(), any(ExecutionInfo.class)))
                .thenReturn(expectedTokenResponseDTO);

        // when
        TokenResponseDTO tokenResponseDTO = restClient.refreshAccessToken(httpClient, refreshTokenRequest, authMeans, TokenResponseDTO.class);

        // then
        assertThat(tokenResponseDTO).isEqualTo(expectedTokenResponseDTO);
        verify(errorHandler).executeAndHandle(executionSupplierArgumentCaptor.capture(), executionInfoArgumentCaptor.capture());
        verify(tokenRequestMapper).mapRefreshTokenRequest(refreshTokenRequest);
        verify(headersFactory).createAccessTokenHeaders(authMeans, expectedTokenRequestBody, refreshTokenRequest);
        assertThat(executionInfoArgumentCaptor.getValue()).isEqualToComparingFieldByField(expectedExecutionInfo);
    }

    private AccessTokenRequest createAccessTokenRequest(DefaultAuthenticationMeans authMeans) {
        return new AccessTokenRequest(TOKEN_URL, authMeans, "authCode", "redirectUrl", DataProviderState.emptyState(), Scope.AISP, signer);
    }

    private RefreshTokenRequest createRefreshTokenRequest(DefaultAuthenticationMeans authMeans) {
        return new RefreshTokenRequest(TOKEN_URL, authMeans, "refreshToken", Scope.AISP, signer);
    }

    private MultiValueMap<String, String> createTokenRequestBody() {
        MultiValueMap<String, String> responseBody = new LinkedMultiValueMap<>();
        responseBody.set("access_token", "accessToken");
        return responseBody;
    }

    private DefaultAuthenticationMeans createDefaultAuthenticationMeans() {
        return DefaultAuthenticationMeans.builder()
                .build();
    }

    private TokenResponseDTO createTokenResponseDTO() {
        return TestTokenResponseDTO.builder()
                .build();
    }
}
