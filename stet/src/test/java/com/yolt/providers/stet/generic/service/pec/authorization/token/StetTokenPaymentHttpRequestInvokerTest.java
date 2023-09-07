package com.yolt.providers.stet.generic.service.pec.authorization.token;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.http.client.HttpClientFactory;
import com.yolt.providers.stet.generic.service.pec.common.StetPaymentHttpHeadersFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

import java.util.Map;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.CLIENT_CREDENTIALS_GRANT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpMethod.POST;

@ExtendWith(MockitoExtension.class)
@Deprecated
class StetTokenPaymentHttpRequestInvokerTest {

    private static final String PROVIDER_IDENTIFIER = "STET_PROVIDER";
    private static final String PROVIDER_DISPLAY_NAME = "Stet Provider";
    private static final String BASE_URL = "https://stetbank.com";
    private static final String TOKEN_URL = BASE_URL + "/oauth2/token";

    @Mock
    private StetTokenPaymentHttpRequestBodyProvider tokenPaymentHttpRequestBodyProvider;

    @Mock
    private HttpClientFactory httpClientFactory;

    @Mock
    private StetPaymentHttpHeadersFactory httpHeadersFactory;

    @Mock
    private RestTemplateManager restTemplateManager;

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

    private StetTokenPaymentHttpRequestInvoker tokenHttpRequestInvoker;

    @BeforeEach
    void setUp() {
        ProviderIdentification providerIdentification = new ProviderIdentification(
                PROVIDER_IDENTIFIER,
                PROVIDER_DISPLAY_NAME,
                ProviderVersion.VERSION_1);

        tokenHttpRequestInvoker = new StetTokenPaymentHttpRequestInvoker(
                tokenPaymentHttpRequestBodyProvider,
                httpHeadersFactory,
                httpClientFactory,
                providerIdentification);
    }

    @Test
    void shouldInvokeRequestAndReturnTokenResponse() throws TokenInvalidException {
        // given
        StetTokenPaymentPreExecutionResult preExecutionResult = StetTokenPaymentPreExecutionResult.builder()
                .requestUrl(TOKEN_URL)
                .baseUrl(BASE_URL)
                .restTemplateManager(restTemplateManager)
                .authMeans(authenticationMeans)
                .build();

        HttpHeaders expectedHttpHeaders = new HttpHeaders();
        expectedHttpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        given(httpClientFactory.createHttpClient(any(RestTemplateManager.class), any(DefaultAuthenticationMeans.class), anyString(), anyString()))
                .willReturn(httpClient);
        given(tokenPaymentHttpRequestBodyProvider.createRequestBody(any(DefaultAuthenticationMeans.class)))
                .willReturn(requestBody);
        given(tokenPaymentHttpRequestBodyProvider.createHttpEntity(any(), any(HttpHeaders.class)))
                .willReturn(httpEntity);
        given(httpHeadersFactory.createPaymentAccessTokenHttpHeaders(eq(preExecutionResult), any()))
                .willReturn(expectedHttpHeaders);
        given(httpClient.exchangeForBody(anyString(), any(HttpMethod.class), any(HttpEntity.class), anyString(), eq(TokenResponseDTO.class)))
                .willReturn(tokenResponseDTO);

        // when
        TokenResponseDTO tokenResponseDTO = tokenHttpRequestInvoker.invokeRequest(preExecutionResult);

        // then
        assertThat(tokenResponseDTO).isNotNull();

        then(httpClientFactory)
                .should()
                .createHttpClient(restTemplateManager, authenticationMeans, BASE_URL, PROVIDER_DISPLAY_NAME);
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
                .exchangeForBody(TOKEN_URL, POST, httpEntity, CLIENT_CREDENTIALS_GRANT, TokenResponseDTO.class);
    }
}
