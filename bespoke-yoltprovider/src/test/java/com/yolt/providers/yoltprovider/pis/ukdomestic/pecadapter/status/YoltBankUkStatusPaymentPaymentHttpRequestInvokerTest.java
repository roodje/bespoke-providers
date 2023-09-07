package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.yoltprovider.pis.ukdomestic.YoltBankUkDomesticHttpService;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit.YoltBankUkSubmitPreExecutionResult;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static com.yolt.providers.common.pis.common.PaymentType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class YoltBankUkStatusPaymentPaymentHttpRequestInvokerTest {

    @InjectMocks
    private YoltBankUkStatusPaymentPaymentHttpRequestInvoker httpRequestInvoker;

    @Mock
    private YoltBankUkDomesticHttpService httpService;

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForInvokeSinglePaymentRequest() {
        // given
        HttpEntity<Void> httpEntity = Mockito.mock(HttpEntity.class);
        UUID paymentId = UUID.randomUUID();
        YoltBankUkSubmitPreExecutionResult yoltBankUkSubmitPreExecutionResult = new YoltBankUkSubmitPreExecutionResult(
                paymentId,
                null,
                SINGLE
        );
        ResponseEntity<JsonNode> responseEntity = ResponseEntity.ok(mock(JsonNode.class));
        given(httpService.getSinglePaymentStatus(httpEntity, paymentId.toString()))
                .willReturn(responseEntity);

        // when
        ResponseEntity<JsonNode> result = httpRequestInvoker.invokeRequest(httpEntity, yoltBankUkSubmitPreExecutionResult);

        // then
        assertThat(result).isEqualTo(responseEntity);
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForInvokeScheduledPaymentRequest() {
        // given
        HttpEntity<Void> httpEntity = Mockito.mock(HttpEntity.class);
        UUID paymentId = UUID.randomUUID();
        YoltBankUkSubmitPreExecutionResult yoltBankUkSubmitPreExecutionResult = new YoltBankUkSubmitPreExecutionResult(
                paymentId,
                null,
                SCHEDULED
        );
        ResponseEntity<JsonNode> responseEntity = ResponseEntity.ok(mock(JsonNode.class));
        given(httpService.getScheduledPaymentStatus(httpEntity, paymentId.toString()))
                .willReturn(responseEntity);

        // when
        ResponseEntity<JsonNode> result = httpRequestInvoker.invokeRequest(httpEntity, yoltBankUkSubmitPreExecutionResult);

        // then
        assertThat(result).isEqualTo(responseEntity);
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForInvokePeriodicPaymentRequest() {
        // given
        HttpEntity<Void> httpEntity = Mockito.mock(HttpEntity.class);
        UUID paymentId = UUID.randomUUID();
        YoltBankUkSubmitPreExecutionResult yoltBankUkSubmitPreExecutionResult = new YoltBankUkSubmitPreExecutionResult(
                paymentId,
                null,
                PERIODIC
        );
        ResponseEntity<JsonNode> responseEntity = ResponseEntity.ok(mock(JsonNode.class));
        given(httpService.getPeriodicPaymentStatus(httpEntity, paymentId.toString()))
                .willReturn(responseEntity);

        // when
        ResponseEntity<JsonNode> result = httpRequestInvoker.invokeRequest(httpEntity, yoltBankUkSubmitPreExecutionResult);

        // then
        assertThat(result).isEqualTo(responseEntity);
    }

    @Test
    void shouldThrowErrorWhenInvokingRequestWithoutPaymentTypeInProviderStatus() {
        // given
        HttpEntity<Void> httpEntity = Mockito.mock(HttpEntity.class);
        UUID paymentId = UUID.randomUUID();
        YoltBankUkSubmitPreExecutionResult yoltBankUkSubmitPreExecutionResult = new YoltBankUkSubmitPreExecutionResult(
                paymentId,
                null,
                null
        );

        // when
        ThrowableAssert.ThrowingCallable getStatusThrowable = () -> httpRequestInvoker.invokeRequest(httpEntity, yoltBankUkSubmitPreExecutionResult);

        // then
        assertThatThrownBy(getStatusThrowable)
                .isInstanceOf(PaymentExecutionTechnicalException.class)
                .hasMessage("status_failed");
    }
}