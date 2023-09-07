package com.yolt.providers.stet.generic.service.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentInitiationRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.INITIATE_PAYMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class StetInitiatePaymentHttpRequestInvokerV2Test {

    private static final String PAYMENT_INITIATION_PATH = "/payment-requests";

    @Mock
    private HttpErrorHandlerV2 httpErrorHandler;

    @Mock
    private DefaultAuthenticationMeans authenticationMeans;

    @Mock
    private HttpClient httpClient;

    private StetInitiatePaymentHttpRequestInvokerV2 initiatePaymentHttpRequestInvoker;

    @BeforeEach
    void initialize() {
        initiatePaymentHttpRequestInvoker = new StetInitiatePaymentHttpRequestInvokerV2(
                httpErrorHandler);
    }

    @Test
    void shouldInvokeRequestAndReturnPaymentInitiationResponseAsJsonNode() throws TokenInvalidException {
        // given
        HttpEntity<StetPaymentInitiationRequestDTO> httpEntity = createHttpEntity();

        StetInitiatePreExecutionResult preExecutionResult = StetInitiatePreExecutionResult.builder()
                .httpMethod(HttpMethod.POST)
                .requestPath(PAYMENT_INITIATION_PATH)
                .authMeans(authenticationMeans)
                .httpClient(httpClient)
                .build();

        JsonNode expectedJsonNode = JsonNodeFactory.instance.textNode("Payment Initiation");
        given(httpClient.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), anyString(), eq(JsonNode.class), any(HttpErrorHandlerV2.class)))
                .willReturn(new ResponseEntity<>(expectedJsonNode, HttpStatus.CREATED));

        // when
        ResponseEntity<JsonNode> responseEntity = initiatePaymentHttpRequestInvoker.invokeRequest(httpEntity, preExecutionResult);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isEqualTo(expectedJsonNode);

        then(httpClient)
                .should()
                .exchange(PAYMENT_INITIATION_PATH, HttpMethod.POST, httpEntity, INITIATE_PAYMENT, JsonNode.class, httpErrorHandler);
    }

    private HttpEntity<StetPaymentInitiationRequestDTO> createHttpEntity() {
        StetPaymentInitiationRequestDTO requestDTO = StetPaymentInitiationRequestDTO.builder()
                .build();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        return new HttpEntity<>(requestDTO, httpHeaders);
    }
}
