package com.yolt.providers.abnamrogroup.common.pis.pec.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.auth.AccessTokenResponseDTO;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroHttpClientFactory;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroPisHttpClient;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroTestPisAuthenticationMeans;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AbnAmroSubmitPaymentHttpRequestInvokerTest {

    @InjectMocks
    private AbnAmroSubmitPaymentHttpRequestInvoker subject;

    @Mock
    private AbnAmroHttpClientFactory httpClientFactory;

    @Mock
    private AbnAmroPisHttpClient httpClient;

    @Mock
    private ResponseEntity<JsonNode> response;

    @Mock
    private HttpEntity<Void> httpEntity;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForInvokeRequestWhenCorrectData() throws TokenInvalidException {
        // given
        AbnAmroAuthenticationMeans authenticationMeans = new AbnAmroAuthenticationMeans(new AbnAmroTestPisAuthenticationMeans().getAuthMeans());
        AbnAmroSubmitPaymentPreExecutionResult preExecutionResult = new AbnAmroSubmitPaymentPreExecutionResult(
                new AccessTokenResponseDTO("accessToken", "", 0, "", ""),
                authenticationMeans,
                restTemplateManager,
                "transactionId"
        );

        given(httpClientFactory.createAbnAmroPisHttpClient(any(RestTemplateManager.class), any(AbnAmroAuthenticationMeans.class)))
                .willReturn(httpClient);
        given(httpClient.submitPayment(any(HttpEntity.class), anyString()))
                .willReturn(response);

        // when
        ResponseEntity<JsonNode> result = subject.invokeRequest(httpEntity, preExecutionResult);

        // then
        then(httpClientFactory)
                .should()
                .createAbnAmroPisHttpClient(restTemplateManager, authenticationMeans);
        then(httpClient)
                .should()
                .submitPayment(httpEntity, "transactionId");
        assertThat(result).isEqualTo(response);
    }

    @Test
    void shouldThrowIllegalStateExceptionWithTokenInvalidExceptionAsCauseWhenTokenInvalidExceptionIsThrownByHttpClient() throws TokenInvalidException {
        // given
        AbnAmroAuthenticationMeans authenticationMeans = new AbnAmroAuthenticationMeans(new AbnAmroTestPisAuthenticationMeans().getAuthMeans());
        AbnAmroSubmitPaymentPreExecutionResult preExecutionResult = new AbnAmroSubmitPaymentPreExecutionResult(
                new AccessTokenResponseDTO("accessToken", "", 0, "", ""),
                authenticationMeans,
                restTemplateManager,
                "transactionId"
        );

        given(httpClientFactory.createAbnAmroPisHttpClient(any(RestTemplateManager.class), any(AbnAmroAuthenticationMeans.class)))
                .willReturn(httpClient);
        given(httpClient.submitPayment(any(HttpEntity.class), anyString()))
                .willThrow(TokenInvalidException.class);

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.invokeRequest(httpEntity, preExecutionResult);

        // then
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(callable)
                .withMessage("Unable to submit payment request")
                .withCauseInstanceOf(TokenInvalidException.class);
    }
}