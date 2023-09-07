package com.yolt.providers.abnamrogroup.common.pis.pec.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroHttpClientFactory;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroPisHttpClient;
import com.yolt.providers.abnamrogroup.common.pis.AbnAmroTestPisAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPaymentProviderState;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AbnAmroPaymentStatusRequestInvokerTest {

    @InjectMocks
    private AbnAmroPaymentStatusRequestInvoker subject;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private AbnAmroHttpClientFactory httpClientFactory;

    @Mock
    private AbnAmroPisHttpClient httpClient;

    @Test
    void shouldReturnResponseEntityWithJsonNodeCallingHttpClientWhenUserAccessTokenIsProvided() throws TokenInvalidException {
        // given
        HttpEntity<Void> httpEntity = new HttpEntity<>(null);
        AbnAmroAuthenticationMeans authenticationMeans = new AbnAmroAuthenticationMeans(new AbnAmroTestPisAuthenticationMeans().getAuthMeans());
        AbnAmroPaymentStatusPreExecutionResult preExecutionResult = new AbnAmroPaymentStatusPreExecutionResult(new AbnAmroPaymentProviderState("transactionId",
                null,
                new AbnAmroPaymentProviderState.UserAccessTokenState("accessToken", "", 0, Clock.systemUTC())),
                authenticationMeans,
                restTemplateManager);
        JsonNode expectedJsonNode = mock(JsonNode.class);

        given(httpClientFactory.createAbnAmroPisHttpClient(any(RestTemplateManager.class), any(AbnAmroAuthenticationMeans.class)))
                .willReturn(httpClient);
        given(httpClient.getPaymentStatus(any(HttpEntity.class), anyString()))
                .willReturn(ResponseEntity.ok(expectedJsonNode));

        // when
        ResponseEntity<JsonNode> result = subject.invokeRequest(httpEntity, preExecutionResult);

        // then
        then(httpClientFactory)
                .should()
                .createAbnAmroPisHttpClient(restTemplateManager, authenticationMeans);
        then(httpClient)
                .should()
                .getPaymentStatus(httpEntity, "transactionId");
        assertThat(result.getBody()).isEqualTo(expectedJsonNode);
    }
}