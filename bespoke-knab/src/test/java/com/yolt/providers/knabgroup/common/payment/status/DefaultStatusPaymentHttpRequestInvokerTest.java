package com.yolt.providers.knabgroup.common.payment.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpErrorHandler;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.knabgroup.common.auth.KnabGroupAuthenticationMeans;
import com.yolt.providers.knabgroup.common.http.KnabGroupHttpClient;
import com.yolt.providers.knabgroup.common.http.KnabGroupHttpClientFactory;
import com.yolt.providers.knabgroup.common.payment.DefaultPisHttpClientErrorHandler;
import com.yolt.providers.knabgroup.common.payment.PaymentEndpointResolver;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.StatusPaymentPreExecutionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static com.yolt.providers.knabgroup.common.payment.PaymentEndpointResolver.STATUS_PAYMENT_ENDPOINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DefaultStatusPaymentHttpRequestInvokerTest {


    private DefaultStatusPaymentHttpRequestInvoker subject;

    @Mock
    private KnabGroupHttpClientFactory httpClientFactory;

    @Mock
    private KnabGroupAuthenticationMeans authMeans;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private KnabGroupHttpClient httpClient;

    @Mock
    private HttpEntity requestEntity;

    @Mock
    private ResponseEntity<JsonNode> responseEntity;


    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForInvokeRequestWhenCorrectData() throws TokenInvalidException {
        // given
        StatusPaymentPreExecutionResult preExecutionResult = createPreExecutionResult();

        subject = new DefaultStatusPaymentHttpRequestInvoker(httpClientFactory, new DefaultPisHttpClientErrorHandler(), new PaymentEndpointResolver());

        given(httpClientFactory.createKnabGroupHttpClient(
                any(RestTemplateManager.class),
                any(KnabGroupAuthenticationMeans.class))
        ).willReturn(httpClient);

        given(httpClient.exchange(
                        eq(STATUS_PAYMENT_ENDPOINT),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(ProviderClientEndpoints.GET_PAYMENT_STATUS),
                        eq(JsonNode.class),
                        any(HttpErrorHandler.class),
                        eq("paymentId")
                )
        ).willReturn(responseEntity);

        // when
        ResponseEntity<JsonNode> result = subject.invokeRequest(requestEntity, preExecutionResult);

        // then
        assertThat(result).isEqualTo(responseEntity);
    }

    private StatusPaymentPreExecutionResult createPreExecutionResult() {
        return new StatusPaymentPreExecutionResult(
                "paymentId",
                restTemplateManager,
                authMeans,
                null,
                null,
                null,
                null
        );
    }
}