package com.yolt.providers.stet.generic.service.pec.confirmation.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.GET_PAYMENT_STATUS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class StetStatusPaymentHttpRequestInvokerV2Test {

    private static final String PAYMENT_INITIATION_PATH = "/payment-requests";

    @Mock
    private HttpErrorHandlerV2 httpErrorHandler;

    @Mock
    private DefaultAuthenticationMeans authenticationMeans;

    @Mock
    private HttpClient httpClient;

    private StetStatusPaymentHttpRequestInvokerV2 stetStatusPaymentHttpRequestInvoker;

    @BeforeEach
    void initialize() {

        stetStatusPaymentHttpRequestInvoker = new StetStatusPaymentHttpRequestInvokerV2(
                httpErrorHandler);
    }

    @Test
    void shouldInvokeRequestAndReturnPaymentStatusResponseAsJsonNode() throws TokenInvalidException {
        // given
        HttpEntity<StetPaymentConfirmationRequestDTO> httpEntity = createHttpEntity();

        StetConfirmationPreExecutionResult preExecutionResult = StetConfirmationPreExecutionResult.builder()
                .httpMethod(HttpMethod.GET)
                .requestPath(PAYMENT_INITIATION_PATH)
                .authMeans(authenticationMeans)
                .httpClient(httpClient)
                .build();

        JsonNode expectedJsonNode = JsonNodeFactory.instance.textNode("Payment Initiation");

        given(httpClient.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), anyString(), eq(JsonNode.class), any(HttpErrorHandlerV2.class)))
                .willReturn(ResponseEntity.ok(expectedJsonNode));

        // when
        ResponseEntity<JsonNode> responseEntity = stetStatusPaymentHttpRequestInvoker.invokeRequest(httpEntity, preExecutionResult);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(expectedJsonNode);

        then(httpClient)
                .should()
                .exchange(PAYMENT_INITIATION_PATH, HttpMethod.GET, httpEntity, GET_PAYMENT_STATUS, JsonNode.class, httpErrorHandler);
    }

    private HttpEntity<StetPaymentConfirmationRequestDTO> createHttpEntity() {
        StetPaymentConfirmationRequestDTO requestDTO = StetPaymentConfirmationRequestDTO.builder()
                .build();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return new HttpEntity<>(requestDTO, httpHeaders);
    }
}
