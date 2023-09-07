package com.yolt.providers.stet.generic.service.pec.authorization.token;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.service.pec.common.StetPaymentHttpHeadersFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;

import java.util.Map;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.CLIENT_CREDENTIALS_GRANT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpMethod.POST;

@ExtendWith(MockitoExtension.class)
class StetTokenPaymentHttpRequestInvokerTestV2 {

    private static final String BASE_URL = "https://stetbank.com";
    private static final String TOKEN_URL = BASE_URL + "/oauth2/token";

    @Mock
    private StetTokenPaymentHttpRequestBodyProvider tokenPaymentHttpRequestBodyProvider;

    @Mock
    private StetPaymentHttpHeadersFactory httpHeadersFactory;

    @Mock
    private DefaultAuthenticationMeans authenticationMeans;

    @Mock
    private DefaultHttpClient httpClient;

    @Mock
    private HttpEntity<Map<String, ?>> httpEntity;

    @Mock
    private MultiValueMap<String, String> requestBody;

    @Mock
    private TokenResponseDTO tokenResponseDTO;

    @Mock
    private HttpErrorHandlerV2 errorhandler;

    private StetTokenPaymentHttpRequestInvokerV2 tokenHttpRequestInvoker;

    @BeforeEach
    void setUp() {
        tokenHttpRequestInvoker = new StetTokenPaymentHttpRequestInvokerV2(
                tokenPaymentHttpRequestBodyProvider,
                httpHeadersFactory,
                errorhandler);
    }

    @Test
    void shouldInvokeRequestAndReturnTokenResponse() throws TokenInvalidException {
        // given
        StetTokenPaymentPreExecutionResult preExecutionResult = StetTokenPaymentPreExecutionResult.builder()
                .requestUrl(TOKEN_URL)
                .httpClient(httpClient)
                .authMeans(authenticationMeans)
                .build();

        HttpHeaders expectedHttpHeaders = new HttpHeaders();
        expectedHttpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        given(tokenPaymentHttpRequestBodyProvider.createRequestBody(any(DefaultAuthenticationMeans.class)))
                .willReturn(requestBody);
        given(tokenPaymentHttpRequestBodyProvider.createHttpEntity(any(), any(HttpHeaders.class)))
                .willReturn(httpEntity);
        given(httpHeadersFactory.createPaymentAccessTokenHttpHeaders(eq(preExecutionResult), any()))
                .willReturn(expectedHttpHeaders);
        given(httpClient.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), anyString(), eq(TokenResponseDTO.class), any(HttpErrorHandlerV2.class)))
                .willReturn(ResponseEntity.ok(tokenResponseDTO));

        // when
        TokenResponseDTO tokenResponseDTO = tokenHttpRequestInvoker.invokeRequest(preExecutionResult);

        // then
        assertThat(tokenResponseDTO).isNotNull();

        then(tokenPaymentHttpRequestBodyProvider)
                .should()
                .createRequestBody(authenticationMeans);
        then(tokenPaymentHttpRequestBodyProvider)
                .should()
                .createHttpEntity(requestBody, expectedHttpHeaders);
        then(httpHeadersFactory)
                .should()
                .createPaymentAccessTokenHttpHeaders(preExecutionResult, requestBody);
        then(httpClient)
                .should()
                .exchange(TOKEN_URL, POST, httpEntity, CLIENT_CREDENTIALS_GRANT, TokenResponseDTO.class, errorhandler);
    }
}
