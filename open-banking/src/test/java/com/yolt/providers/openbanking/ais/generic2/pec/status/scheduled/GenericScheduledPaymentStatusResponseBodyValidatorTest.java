package com.yolt.providers.openbanking.ais.generic2.pec.status.scheduled;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.TextNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.ScheduledPaymentStatusResponse;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
class GenericScheduledPaymentStatusResponseBodyValidatorTest {

    @InjectMocks
    private GenericScheduledPaymentStatusResponseBodyValidator subject;

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenPaymentStatusResponseIsNull() {
        // given
        TextNode rawResponseBody = JsonNodeFactory.instance.textNode("fake");

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(null, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Payment status response is null");
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenDataInPaymentStatusResponseIsNull() {
        // given
        TextNode rawResponseBody = JsonNodeFactory.instance.textNode("fake");
        ScheduledPaymentStatusResponse paymentStatusResponse = new ScheduledPaymentStatusResponse();

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(paymentStatusResponse, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Payment status response data is null");
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenStatusInDataIsNull() {
        // given
        TextNode rawResponseBody = JsonNodeFactory.instance.textNode("fake");
        ScheduledPaymentStatusResponse paymentStatusResponse = new ScheduledPaymentStatusResponse();
        paymentStatusResponse.setData(new ScheduledPaymentStatusResponse.Data("", "", null));

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(paymentStatusResponse, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Payment status is null");
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenResourceInDataIsNull() {
        // given
        TextNode rawResponseBody = JsonNodeFactory.instance.textNode("fake");
        ScheduledPaymentStatusResponse paymentStatusResponse = new ScheduledPaymentStatusResponse();
        paymentStatusResponse.setData(new ScheduledPaymentStatusResponse.Data(null, null, ScheduledPaymentStatusResponse.Data.Status.INITIATIONPENDING));

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(paymentStatusResponse, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Resource ID is null or empty");
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenResourceInDataIsEmpty() {
        // given
        TextNode rawResponseBody = JsonNodeFactory.instance.textNode("fake");
        ScheduledPaymentStatusResponse paymentStatusResponse = new ScheduledPaymentStatusResponse();
        paymentStatusResponse.setData(new ScheduledPaymentStatusResponse.Data("", "", ScheduledPaymentStatusResponse.Data.Status.INITIATIONPENDING));

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(paymentStatusResponse, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Resource ID is null or empty");
    }
}