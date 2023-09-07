package com.yolt.providers.rabobank.pis.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.rabobank.RabobankAuthenticationMeans;
import com.yolt.providers.rabobank.RabobankSampleTypedAuthenticationMeans;
import com.yolt.providers.rabobank.dto.external.SepaCreditTransfer;
import com.yolt.providers.rabobank.pis.pec.RabobankPisHttpClient;
import com.yolt.providers.rabobank.pis.pec.RabobankPisHttpClientFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabobankSepaInitiatePaymentHttpRequestInvokerTest {

    private final RabobankSampleTypedAuthenticationMeans sampleTypedAuthenticationMeans = new RabobankSampleTypedAuthenticationMeans();

    @InjectMocks
    private RabobankSepaInitiatePaymentHttpRequestInvoker subject;

    @Mock
    private RabobankPisHttpClientFactory httpClientFactory;

    @Mock
    RabobankPisHttpClient httpClient;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Test
    void shouldPerformHttpRequestWithGivenBody() throws IOException, URISyntaxException, TokenInvalidException {
        //given
        RabobankAuthenticationMeans authenticationMeans = RabobankAuthenticationMeans.fromPISAuthenticationMeans(sampleTypedAuthenticationMeans.getRabobankSampleTypedAuthenticationMeans());
        RabobankSepaInitiatePreExecutionResult preExecutionResult = preparePreExecutionResult(authenticationMeans);
        HttpEntity<SepaCreditTransfer> httpRequestEntity = mock(HttpEntity.class);
        when(httpClientFactory.createRabobankPisHttpClient(restTemplateManager, authenticationMeans)).thenReturn(httpClient);
        ResponseEntity<JsonNode> expectedResponseEntity = mock(ResponseEntity.class);
        when(httpClient.initiatePayment(httpRequestEntity)).thenReturn(expectedResponseEntity);

        //when
        ResponseEntity<JsonNode> receivedResponse = subject.invokeRequest(httpRequestEntity, preExecutionResult);

        //then
        assertThat(receivedResponse).isEqualTo(expectedResponseEntity);
    }

    private RabobankSepaInitiatePreExecutionResult preparePreExecutionResult(RabobankAuthenticationMeans authenticationMeans) throws IOException, URISyntaxException {
        return new RabobankSepaInitiatePreExecutionResult(authenticationMeans,
                null,
                "http://yolt.com/callback",
                null,
                null,
                restTemplateManager,
                null);
    }
}
