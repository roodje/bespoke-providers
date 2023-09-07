package com.yolt.providers.ing.common.pec.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.exception.DefaultPisHttpClientErrorHandler;
import com.yolt.providers.ing.common.http.HttpClientFactory;
import com.yolt.providers.ing.common.pec.IngPecConstants;
import com.yolt.providers.ing.common.pec.submit.DefaultSubmitPaymentPreExecutionResult;
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
class DefaultStatusPaymentHttpRequestInvokerTest {


    private DefaultStatusPaymentHttpRequestInvoker sut;

    @Mock
    private HttpClientFactory httpClientFactory;

    @Mock
    private IngAuthenticationMeans authMeans;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpEntity<Void> requestEntity;

    @Mock
    private ResponseEntity<JsonNode> responseEntity;


    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForInvokeRequestWhenCorrectData() throws TokenInvalidException {
        // given
        var preExecutionResult = createPreExecutionResult();
        var expectedPath = String.format(IngPecConstants.SUBMIT_PAYMENT_ENDPOINT, "fakePaymentId");

        sut = new DefaultStatusPaymentHttpRequestInvoker(httpClientFactory, new DefaultPisHttpClientErrorHandler(), "");

        given(httpClientFactory.createPisHttpClient(
                any(IngAuthenticationMeans.class),
                any(RestTemplateManager.class),
                anyString()
                )
        ).willReturn(httpClient);

        given(httpClient.exchange(
                eq(expectedPath),
                eq(IngPecConstants.SUBMIT_PAYMENT_HTTP_METHOD),
                any(HttpEntity.class),
                eq(ProviderClientEndpoints.GET_PAYMENT_STATUS),
                eq(JsonNode.class),
                any(HttpErrorHandler.class)
                )
        ).willReturn(responseEntity);

        // when
        var result = sut.invokeRequest(requestEntity, preExecutionResult);

        // then
        assertThat(result).isEqualTo(responseEntity);
    }

    private DefaultSubmitPaymentPreExecutionResult createPreExecutionResult() {
        return new DefaultSubmitPaymentPreExecutionResult(
                "fakePaymentId",
                restTemplateManager,
                authMeans,
                null,
                null,
                null,
                null
        );
    }

}