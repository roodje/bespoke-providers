package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.yoltprovider.pis.sepa.YoltBankSepaPaymentHttpService;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static com.yolt.providers.common.pis.common.PaymentType.PERIODIC;
import static com.yolt.providers.common.pis.common.PaymentType.SINGLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class YoltBankSepaSubmitPaymentPaymentHttpRequestInvokerTest {

    @InjectMocks
    private YoltBankSepaSubmitPaymentPaymentHttpRequestInvoker httpRequestInvoker;

    @Mock
    private YoltBankSepaPaymentHttpService httpService;

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForInvokeRequestSinglePayment() {
        // given
        HttpEntity<Void> httpEntity = new HttpEntity<>(null);
        YoltBankSepaSubmitPreExecutionResult preExecutionResult = new YoltBankSepaSubmitPreExecutionResult(
                "fakePaymentId",
                null,
                null,
                SINGLE
        );
        JsonNode fakeResponseBody = mock(JsonNode.class);
        given(httpService.postSubmitSinglePaymentRequest(httpEntity, "fakePaymentId"))
                .willReturn(ResponseEntity.ok(fakeResponseBody));

        // when
        ResponseEntity<JsonNode> result = httpRequestInvoker.invokeRequest(httpEntity, preExecutionResult);

        // then
        assertThat(result)
                .extracting(HttpEntity::getBody)
                .isEqualTo(fakeResponseBody);
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForInvokeRequestPeriodicPayment() {
        // given
        HttpEntity<Void> httpEntity = new HttpEntity<>(null);
        YoltBankSepaSubmitPreExecutionResult preExecutionResult = new YoltBankSepaSubmitPreExecutionResult(
                "fakePaymentId",
                null,
                null,
                PERIODIC
        );
        JsonNode fakeResponseBody = mock(JsonNode.class);
        given(httpService.postSubmitPeriodicPaymentRequest(httpEntity, "fakePaymentId"))
                .willReturn(ResponseEntity.ok(fakeResponseBody));

        // when
        ResponseEntity<JsonNode> result = httpRequestInvoker.invokeRequest(httpEntity, preExecutionResult);

        // then
        assertThat(result)
                .extracting(HttpEntity::getBody)
                .isEqualTo(fakeResponseBody);
    }

    @Test
    void shouldThrowErrorWhenInvokingSubmitPaymentWithoutPaymentTypeInProviderState() {
        // given
        HttpEntity<Void> httpEntity = new HttpEntity<>(null);
        YoltBankSepaSubmitPreExecutionResult preExecutionResult = new YoltBankSepaSubmitPreExecutionResult(
                "fakePaymentId",
                null,
                null,
                null
        );

        // when
        ThrowableAssert.ThrowingCallable submitThrowable = () -> httpRequestInvoker.invokeRequest(httpEntity, preExecutionResult);

        // then
        assertThatThrownBy(submitThrowable)
                .isInstanceOf(PaymentExecutionTechnicalException.class)
                .hasMessage("submit_preparation_error");
    }
}
