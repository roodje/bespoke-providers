package com.yolt.providers.abnamrogroup.common.pis.pec.submit;

import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.auth.AccessTokenResponseDTO;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroTestPisAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPaymentProviderState;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPisAccessTokenProvider;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroProviderStateDeserializer;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequestBuilder;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MultiValueMap;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AbnAmroSubmitPaymentPreExecutionResultMapperTest {

    @InjectMocks
    private AbnAmroSubmitPaymentPreExecutionResultMapper subject;

    @Mock
    private AbnAmroPisAccessTokenProvider pisAccessTokenProvider;

    @Mock
    private AbnAmroAuthorizationCodeExtractor authorizationCodeExtractor;

    @Mock
    private AbnAmroProviderStateDeserializer providerStateDeserializer;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Captor
    private ArgumentCaptor<AbnAmroAuthenticationMeans> authenticationMeansArgumentCaptor;

    @Captor
    private ArgumentCaptor<MultiValueMap<String, String>> bodyArgumentCaptor;

    @Test
    void shouldReturnAbnAmroPaymentSubmitionPreExecutionResultForMapWhenCorrectData() throws TokenInvalidException {
        // given
        Map<String, BasicAuthenticationMean> authMeans = new AbnAmroTestPisAuthenticationMeans().getAuthMeans();
        SubmitPaymentRequest submitPaymentRequest = createSubmitPaymentRequest(authMeans);
        AccessTokenResponseDTO accessTokenResponseDTO = createAccessTokenResponseDTO();
        AbnAmroPaymentProviderState providerState = new AbnAmroPaymentProviderState("transactionId", "redirectUri", null);

        given(providerStateDeserializer.deserialize(anyString()))
                .willReturn(providerState);
        given(authorizationCodeExtractor.extractAuthorizationCode(anyString()))
                .willReturn("fakeCode");
        given(pisAccessTokenProvider.provideAccessToken(any(RestTemplateManager.class), any(AbnAmroAuthenticationMeans.class), any(MultiValueMap.class)))
                .willReturn(accessTokenResponseDTO);

        // when
        AbnAmroSubmitPaymentPreExecutionResult result = subject.map(submitPaymentRequest);

        // then
        then(providerStateDeserializer)
                .should()
                .deserialize("providerState");
        then(authorizationCodeExtractor)
                .should()
                .extractAuthorizationCode("http://localhost/callback");
        then(pisAccessTokenProvider)
                .should()
                .provideAccessToken(eq(restTemplateManager), authenticationMeansArgumentCaptor.capture(), bodyArgumentCaptor.capture());
        AbnAmroAuthenticationMeans capturedAuthMeans = authenticationMeansArgumentCaptor.getValue();
        assertThat(capturedAuthMeans.getApiKey())
                .isEqualTo(authMeans.get(AbnAmroAuthenticationMeans.API_KEY_NAME).getValue());
        MultiValueMap<String, String> capturedBody = bodyArgumentCaptor.getValue();
        assertThat(capturedBody.toSingleValueMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "grant_type", "authorization_code",
                        "client_id", "TPP_test",
                        "code", "fakeCode",
                        "redirect_uri", "redirectUri"
                ));

        assertThat(result.getAccessTokenResponseDTO().getAccessToken()).isEqualTo("accessToken");
        assertThat(result.getAuthenticationMeans()).isEqualTo(capturedAuthMeans);
        assertThat(result.getRestTemplateManager()).isEqualTo(restTemplateManager);
        assertThat(result.getTransactionId()).isEqualTo("transactionId");
    }

    @Test
    void shouldThrowIllegalStateExceptionWithTokenInvalidExceptionAsCauseWhenTokenInvalidExceptionIsThrownByHttpClient() throws TokenInvalidException {
        // given
        Map<String, BasicAuthenticationMean> authMeans = new AbnAmroTestPisAuthenticationMeans().getAuthMeans();
        SubmitPaymentRequest submitPaymentRequest = createSubmitPaymentRequest(authMeans);
        AbnAmroPaymentProviderState providerState = new AbnAmroPaymentProviderState("transactionId", "redirectUri", null);

        given(providerStateDeserializer.deserialize(anyString()))
                .willReturn(providerState);
        given(authorizationCodeExtractor.extractAuthorizationCode(anyString()))
                .willReturn("fakeCode");
        given(pisAccessTokenProvider.provideAccessToken(any(RestTemplateManager.class), any(AbnAmroAuthenticationMeans.class), any(MultiValueMap.class)))
                .willThrow(TokenInvalidException.class);

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.map(submitPaymentRequest);

        // then
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(callable)
                .withMessage("Unable to exchange authorization code for token")
                .withCauseInstanceOf(TokenInvalidException.class);
    }

    private AccessTokenResponseDTO createAccessTokenResponseDTO() {
        return new AccessTokenResponseDTO("accessToken", "", 0, "", "");
    }

    private SubmitPaymentRequest createSubmitPaymentRequest(Map<String, BasicAuthenticationMean> authMeans) {
        return new SubmitPaymentRequestBuilder()
                .setAuthenticationMeans(authMeans)
                .setRestTemplateManager(restTemplateManager)
                .setProviderState("providerState")
                .setRedirectUrlPostedBackFromSite("http://localhost/callback")
                .build();
    }
}