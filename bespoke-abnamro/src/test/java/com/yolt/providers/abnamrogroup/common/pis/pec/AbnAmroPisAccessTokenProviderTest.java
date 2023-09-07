package com.yolt.providers.abnamrogroup.common.pis.pec;

import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.auth.AccessTokenResponseDTO;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroHttpClientFactory;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroPisHttpClient;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroTestPisAuthenticationMeans;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AbnAmroPisAccessTokenProviderTest {

    @InjectMocks
    private AbnAmroPisAccessTokenProvider subject;

    @Mock
    private AbnAmroHttpClientFactory httpClientFactory;

    @Mock
    private AbnAmroAuthorizationHttpHeadersProvider authorizationHttpHeadersProvider;

    @Mock
    private AbnAmroPisHttpClient httpClient;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Captor
    private ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> httpEntityArgumentCaptor;

    @Test
    void shouldReturnAccessTokenResponseDTOForProvideAccessTokenWhenCorrectData() throws TokenInvalidException {
        // given
        AbnAmroAuthenticationMeans authenticationMeans = new AbnAmroAuthenticationMeans(new AbnAmroTestPisAuthenticationMeans().getAuthMeans());
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        HttpHeaders httpHeaders = new HttpHeaders();

        given(httpClientFactory.createAbnAmroPisHttpClient(any(RestTemplateManager.class), any(AbnAmroAuthenticationMeans.class)))
                .willReturn(httpClient);
        given(authorizationHttpHeadersProvider.provideHttpHeadersForPisToken())
                .willReturn(httpHeaders);
        AccessTokenResponseDTO expectedResult = new AccessTokenResponseDTO();
        given(httpClient.getPisAccessToken(any(HttpEntity.class)))
                .willReturn(expectedResult);

        // when
        AccessTokenResponseDTO result = subject.provideAccessToken(restTemplateManager, authenticationMeans, body);

        // then
        then(httpClientFactory)
                .should()
                .createAbnAmroPisHttpClient(restTemplateManager, authenticationMeans);
        then(authorizationHttpHeadersProvider)
                .should()
                .provideHttpHeadersForPisToken();
        then(httpClient)
                .should()
                .getPisAccessToken(httpEntityArgumentCaptor.capture());
        HttpEntity<MultiValueMap<String, String>> capturedHttpEntity = httpEntityArgumentCaptor.getValue();
        assertThat(capturedHttpEntity.getHeaders()).isEqualTo(httpHeaders);
        assertThat(capturedHttpEntity.getBody()).isEqualTo(body);

        assertThat(result).isEqualTo(expectedResult);
    }
}