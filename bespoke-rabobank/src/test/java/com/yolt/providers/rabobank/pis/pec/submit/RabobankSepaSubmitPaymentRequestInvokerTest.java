package com.yolt.providers.rabobank.pis.pec.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.rabobank.RabobankAuthenticationMeans;
import com.yolt.providers.rabobank.pis.pec.RabobankPisHttpClient;
import com.yolt.providers.rabobank.pis.pec.RabobankPisHttpClientFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabobankSepaSubmitPaymentRequestInvokerTest {

    @InjectMocks
    private RabobankSepaSubmitPaymentRequestInvoker subject;

    @Mock
    private RabobankPisHttpClient httpClient;

    @Mock
    RabobankPisHttpClientFactory httpClientFactory;

    @Mock
    private RabobankAuthenticationMeans authenticationMeans;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Test
    void shouldReturnJsonNodeResponseWhenNoErrorsOccurred() throws TokenInvalidException {
        //given
        RabobankSepaSubmitPaymentPreExecutionResult preExecutionResult = getPreExecutionResult();
        when(httpClientFactory.createRabobankPisHttpClient(restTemplateManager, authenticationMeans)).thenReturn(httpClient);
        HttpEntity<Void> requestEntity = mock(HttpEntity.class);
        ResponseEntity<JsonNode> expectedResponseEntity = mock(ResponseEntity.class);
        when(httpClient.getStatus(requestEntity, "123-456-999")).thenReturn(expectedResponseEntity);

        //when
        ResponseEntity<JsonNode> receivedResponse = subject.invokeRequest(requestEntity, preExecutionResult);
        //then

        assertThat(receivedResponse).isEqualTo(expectedResponseEntity);
    }

    private RabobankSepaSubmitPaymentPreExecutionResult getPreExecutionResult() {
        return new RabobankSepaSubmitPaymentPreExecutionResult("123-456-999",
                authenticationMeans,
                restTemplateManager,
                null,
                null
        );
    }
}
