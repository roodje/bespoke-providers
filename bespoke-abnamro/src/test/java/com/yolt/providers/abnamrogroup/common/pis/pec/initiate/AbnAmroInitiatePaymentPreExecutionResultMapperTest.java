package com.yolt.providers.abnamrogroup.common.pis.pec.initiate;

import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.auth.AccessTokenResponseDTO;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroTestPisAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPisAccessTokenProvider;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MultiValueMap;

import java.util.Map;

import static com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans.API_KEY_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AbnAmroInitiatePaymentPreExecutionResultMapperTest {

    @InjectMocks
    private AbnAmroInitiatePaymentPreExecutionResultMapper subject;

    @Mock
    private AbnAmroPisAccessTokenProvider pisAccessTokenProvider;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Captor
    private ArgumentCaptor<MultiValueMap<String, String>> bodyArgumentCaptor;

    @Captor
    private ArgumentCaptor<AbnAmroAuthenticationMeans> authenticationMeansArgumentCaptor;

    @Test
    void shouldReturnAbnAmroPaymentInitiationPreExecutionResultForMapWhenCorrectData() throws TokenInvalidException {
        // given
        Map<String, BasicAuthenticationMean> authMeans = new AbnAmroTestPisAuthenticationMeans().getAuthMeans();
        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder().build();
        InitiatePaymentRequest initiatePaymentRequest = createInitiatePaymentRequest(authMeans, requestDTO);
        AccessTokenResponseDTO accessTokenResponseDTO = createAccessTokenResponseDTO();

        given(pisAccessTokenProvider.provideAccessToken(any(RestTemplateManager.class), any(AbnAmroAuthenticationMeans.class), any(MultiValueMap.class)))
                .willReturn(accessTokenResponseDTO);

        // when
        AbnAmroInitiatePaymentPreExecutionResult result = subject.map(initiatePaymentRequest);

        // then
        then(pisAccessTokenProvider)
                .should()
                .provideAccessToken(eq(restTemplateManager), authenticationMeansArgumentCaptor.capture(), bodyArgumentCaptor.capture());
        AbnAmroAuthenticationMeans capturedAuthMeans = authenticationMeansArgumentCaptor.getValue();
        assertThat(capturedAuthMeans).extracting(AbnAmroAuthenticationMeans::getClientId,
                AbnAmroAuthenticationMeans::getApiKey)
                .contains(authMeans.get(API_KEY_NAME).getValue());
        MultiValueMap<String, String> capturedBody = bodyArgumentCaptor.getValue();
        assertThat(capturedBody.toSingleValueMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "grant_type", "client_credentials",
                        "client_id", "TPP_test",
                        "scope", "psd2:payment:sepa:write"
                ));

        assertThat(result).extracting(AbnAmroInitiatePaymentPreExecutionResult::getAccessToken,
                AbnAmroInitiatePaymentPreExecutionResult::getAuthenticationMeans,
                AbnAmroInitiatePaymentPreExecutionResult::getRestTemplateManager,
                AbnAmroInitiatePaymentPreExecutionResult::getRequestDTO,
                AbnAmroInitiatePaymentPreExecutionResult::getBaseClientRedirectUrl,
                AbnAmroInitiatePaymentPreExecutionResult::getState)
                .contains("fakeAccessToken", restTemplateManager, requestDTO, "baseClientRedirectUrl", "state");
    }

    private AccessTokenResponseDTO createAccessTokenResponseDTO() {
        return new AccessTokenResponseDTO(
                "fakeAccessToken", "", 0, "", ""
        );
    }

    private InitiatePaymentRequest createInitiatePaymentRequest(Map<String, BasicAuthenticationMean> authMeans, SepaInitiatePaymentRequestDTO requestDTO) {
        return new InitiatePaymentRequest(
                requestDTO,
                "baseClientRedirectUrl",
                "state",
                authMeans,
                null,
                restTemplateManager,
                "",
                null
        );
    }
}