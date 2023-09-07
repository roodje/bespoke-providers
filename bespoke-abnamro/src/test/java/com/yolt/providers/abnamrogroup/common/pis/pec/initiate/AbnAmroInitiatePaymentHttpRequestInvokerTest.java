package com.yolt.providers.abnamrogroup.common.pis.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroHttpClientFactory;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroPisHttpClient;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroTestPisAuthenticationMeans;
import com.yolt.providers.abnamro.pis.SepaPayment;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AbnAmroInitiatePaymentHttpRequestInvokerTest {

    @InjectMocks
    private AbnAmroInitiatePaymentHttpRequestInvoker subject;

    @Mock
    private AbnAmroHttpClientFactory httpClientFactory;

    @Mock
    private AbnAmroPisHttpClient httpClient;

    @Mock
    private HttpEntity<SepaPayment> httpEntity;

    @Mock
    private ResponseEntity<JsonNode> expectedResponse;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForInvokeRequestWhenCorrectData() throws TokenInvalidException {
        // given
        AbnAmroAuthenticationMeans authenticationMeans = new AbnAmroAuthenticationMeans(new AbnAmroTestPisAuthenticationMeans().getAuthMeans());
        AbnAmroInitiatePaymentPreExecutionResult preExecutionResult = createPreExecutionResult(authenticationMeans);

        given(httpClientFactory.createAbnAmroPisHttpClient(any(RestTemplateManager.class), any(AbnAmroAuthenticationMeans.class)))
                .willReturn(httpClient);
        given(httpClient.initiatePayment(any(HttpEntity.class)))
                .willReturn(expectedResponse);

        // when
        ResponseEntity<JsonNode> result = subject.invokeRequest(httpEntity, preExecutionResult);

        // then
        then(httpClientFactory)
                .should()
                .createAbnAmroPisHttpClient(restTemplateManager, authenticationMeans);
        then(httpClient)
                .should()
                .initiatePayment(httpEntity);
        assertThat(result).isEqualTo(expectedResponse);
    }

    private AbnAmroInitiatePaymentPreExecutionResult createPreExecutionResult(AbnAmroAuthenticationMeans authenticationMeans) {
        return new AbnAmroInitiatePaymentPreExecutionResult(
                "",
                authenticationMeans,
                restTemplateManager,
                null,
                "",
                ""
        );
    }
}