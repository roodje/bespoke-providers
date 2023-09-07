package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.yoltprovider.pis.sepa.YoltBankSepaPaymentHttpService;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit.YoltBankSepaSubmitPreExecutionResult;
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
public class YoltBankSepaStatusPaymentPaymentHttpRequestInvokerTest {

    @InjectMocks
    private YoltBankSepaStatusPaymentPaymentHttpRequestInvoker httpRequestInvoker;

    @Mock
    private YoltBankSepaPaymentHttpService httpService;

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForInvokeSinglePaymentRequest() {
        // given
        HttpEntity<Void> httpEntity = new HttpEntity<>(null);
        YoltBankSepaSubmitPreExecutionResult preExecutionResult = new YoltBankSepaSubmitPreExecutionResult(
                "fakePaymentId",
                null,
                null,
                SINGLE
        );
        JsonNode fakeResponseBody = mock(JsonNode.class);
        given(httpService.getSingleStatus(httpEntity, "fakePaymentId"))
                .willReturn(ResponseEntity.ok(fakeResponseBody));

        // when
        ResponseEntity<JsonNode> result = httpRequestInvoker.invokeRequest(httpEntity, preExecutionResult);

        // then

        assertThat(result)
                .extracting(HttpEntity::getBody)
                .isEqualTo(fakeResponseBody);
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForInvokePeriodicPaymentRequest() {
        // given
        HttpEntity<Void> httpEntity = new HttpEntity<>(null);
        YoltBankSepaSubmitPreExecutionResult preExecutionResult = new YoltBankSepaSubmitPreExecutionResult(
                "fakePaymentId",
                null,
                null,
                PERIODIC
        );
        JsonNode fakeResponseBody = mock(JsonNode.class);
        given(httpService.getPeriodicStatus(httpEntity, "fakePaymentId"))
                .willReturn(ResponseEntity.ok(fakeResponseBody));

        // when
        ResponseEntity<JsonNode> result = httpRequestInvoker.invokeRequest(httpEntity, preExecutionResult);

        // then
        assertThat(result)
                .extracting(HttpEntity::getBody)
                .isEqualTo(fakeResponseBody);
    }

    @Test
    void shouldThrowErrorWhenInvokingGetStatusWithoutPaymentTypeInProviderState() {
        // given
        HttpEntity<Void> httpEntity = new HttpEntity<>(null);
        YoltBankSepaSubmitPreExecutionResult preExecutionResult = new YoltBankSepaSubmitPreExecutionResult(
                "fakePaymentId",
                null,
                null,
                null
        );

        // when
        ThrowableAssert.ThrowingCallable getStatusThrowable = () -> httpRequestInvoker.invokeRequest(httpEntity, preExecutionResult);

        // then
        assertThatThrownBy(getStatusThrowable)
                .isInstanceOf(PaymentExecutionTechnicalException.class)
                .hasMessage("status_failed");
    }
}
