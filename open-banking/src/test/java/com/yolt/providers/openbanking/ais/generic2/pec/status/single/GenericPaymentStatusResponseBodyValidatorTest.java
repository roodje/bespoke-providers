package com.yolt.providers.openbanking.ais.generic2.pec.status.single;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.TextNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.PaymentStatusResponse;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
class GenericPaymentStatusResponseBodyValidatorTest {

    @InjectMocks
    private GenericPaymentStatusResponseBodyValidator subject;

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
        PaymentStatusResponse paymentStatusResponse = new PaymentStatusResponse();

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
        PaymentStatusResponse paymentStatusResponse = new PaymentStatusResponse();
        paymentStatusResponse.setData(new PaymentStatusResponse.Data("", "", null));

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
        PaymentStatusResponse paymentStatusResponse = new PaymentStatusResponse();
        paymentStatusResponse.setData(new PaymentStatusResponse.Data(null, null, PaymentStatusResponse.Data.Status.PENDING));

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
        PaymentStatusResponse paymentStatusResponse = new PaymentStatusResponse();
        paymentStatusResponse.setData(new PaymentStatusResponse.Data("", "", PaymentStatusResponse.Data.Status.PENDING));

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(paymentStatusResponse, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Resource ID is null or empty");
    }
}