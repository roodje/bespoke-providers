package com.yolt.providers.ing.common.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.dto.SepaCreditTransfer;
import com.yolt.providers.ing.common.exception.DefaultPisHttpClientErrorHandler;
import com.yolt.providers.ing.common.http.HttpClientFactory;
import com.yolt.providers.ing.common.pec.IngPecConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DefaultInitiatePaymentHttpRequestInvokerTest {


    private DefaultInitiatePaymentHttpRequestInvoker sut;

    @Mock
    private HttpClientFactory httpClientFactory;

    @Mock
    private IngAuthenticationMeans authMeans;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpEntity<SepaCreditTransfer> requestEntity;

    @Mock
    private ResponseEntity<JsonNode> responseEntity;


    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForInvokeRequestWhenCorrectData() throws TokenInvalidException {
        // given
        var preExecutionResult = createPreExecutionResult();

        sut = new DefaultInitiatePaymentHttpRequestInvoker(httpClientFactory, new DefaultPisHttpClientErrorHandler(), "");

        given(httpClientFactory.createPisHttpClient(
                any(IngAuthenticationMeans.class),
                any(RestTemplateManager.class),
                anyString()
                )
        ).willReturn(httpClient);

        given(httpClient.exchange(
                eq(IngPecConstants.INITIATE_PAYMENT_ENDPOINT),
                eq(IngPecConstants.INITIATE_PAYMENT_HTTP_METHOD),
                any(HttpEntity.class),
                eq(ProviderClientEndpoints.INITIATE_PAYMENT),
                eq(JsonNode.class),
                any(HttpErrorHandler.class)
                )
        ).willReturn(responseEntity);

        // when
        var result = sut.invokeRequest(requestEntity, preExecutionResult);

        // then
        assertThat(result).isEqualTo(responseEntity);
    }

    private DefaultInitiatePaymentPreExecutionResult createPreExecutionResult() {
        return new DefaultInitiatePaymentPreExecutionResult(
                null,
                restTemplateManager,
                authMeans,
                null,
                null,
                null,
                null,
                null
        );
    }
}