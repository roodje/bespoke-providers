package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.yoltprovider.pis.ukdomestic.ConfirmPaymentRequest;
import com.yolt.providers.yoltprovider.pis.ukdomestic.YoltBankUkDomesticHttpService;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static com.yolt.providers.common.pis.common.PaymentType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class YoltBankUkSubmitPaymentPaymentHttpRequestInvokerTest {

    @InjectMocks
    private YoltBankUkSubmitPaymentPaymentHttpRequestInvoker subject;

    @Mock
    private YoltBankUkDomesticHttpService httpService;

    @Test
    void shouldReturnCorrectResponseForInvokeSinglePaymentRequest() {
        // given
        YoltBankUkSubmitPreExecutionResult mockResult = new YoltBankUkSubmitPreExecutionResult(
                null,
                null,
                SINGLE);
        HttpEntity<ConfirmPaymentRequest> httpEntity = mock(HttpEntity.class);
        ResponseEntity<JsonNode> responseEntity = ResponseEntity.ok(mock(JsonNode.class));
        given(httpService.postSubmitSinglePayment(httpEntity))
                .willReturn(responseEntity);

        // when
        ResponseEntity<JsonNode> result = subject.invokeRequest(httpEntity, mockResult);

        // then
        assertThat(result).isEqualTo(responseEntity);
    }

    @Test
    void shouldReturnCorrectResponseForInvokeScheduledPaymentRequest() {
        // given
        YoltBankUkSubmitPreExecutionResult mockResult = new YoltBankUkSubmitPreExecutionResult(
                null,
                null,
                SCHEDULED);
        HttpEntity<ConfirmPaymentRequest> httpEntity = mock(HttpEntity.class);
        ResponseEntity<JsonNode> responseEntity = ResponseEntity.ok(mock(JsonNode.class));
        given(httpService.postSubmitScheduledPayment(httpEntity))
                .willReturn(responseEntity);

        // when
        ResponseEntity<JsonNode> result = subject.invokeRequest(httpEntity, mockResult);

        // then
        assertThat(result).isEqualTo(responseEntity);
    }

    @Test
    void shouldReturnCorrectResponseForInvokePeriodicPaymentRequest() {
        // given
        YoltBankUkSubmitPreExecutionResult mockResult = new YoltBankUkSubmitPreExecutionResult(
                null,
                null,
                PERIODIC);
        HttpEntity<ConfirmPaymentRequest> httpEntity = mock(HttpEntity.class);
        ResponseEntity<JsonNode> responseEntity = ResponseEntity.ok(mock(JsonNode.class));
        given(httpService.postSubmitPeriodicPayment(httpEntity))
                .willReturn(responseEntity);

        // when
        ResponseEntity<JsonNode> result = subject.invokeRequest(httpEntity, mockResult);

        // then
        assertThat(result).isEqualTo(responseEntity);
    }

    @Test
    void shouldThrowErrorForInvokePaymentRequestWithoutPaymentType() {
        // given
        YoltBankUkSubmitPreExecutionResult mockResult = new YoltBankUkSubmitPreExecutionResult(
                null,
                null,
                null);
        HttpEntity<ConfirmPaymentRequest> httpEntity = mock(HttpEntity.class);

        // when
        ThrowableAssert.ThrowingCallable getStatusThrowable = () -> subject.invokeRequest(httpEntity, mockResult);

        // then
        assertThatThrownBy(getStatusThrowable)
                .isInstanceOf(PaymentExecutionTechnicalException.class)
                .hasMessage("submit_preparation_error");
    }
}