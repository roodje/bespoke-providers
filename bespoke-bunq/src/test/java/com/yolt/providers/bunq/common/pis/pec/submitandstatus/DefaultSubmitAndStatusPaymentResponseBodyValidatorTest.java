package com.yolt.providers.bunq.common.pis.pec.submitandstatus;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentStatusResponse;
import com.yolt.providers.bunq.common.model.PaymentStatusValue;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DefaultSubmitAndStatusPaymentResponseBodyValidatorTest {

    private final DefaultSubmitAndStatusPaymentResponseBodyValidator responseBodyValidator = new DefaultSubmitAndStatusPaymentResponseBodyValidator();

    @Test
    void shouldNotThrowAnyExceptionWhenCorrectDataAreProvided() {
        //given
        var responseBody = new PaymentServiceProviderDraftPaymentStatusResponse(PaymentStatusValue.ACCEPTED);

        //when
        ThrowableAssert.ThrowingCallable call = () -> responseBodyValidator.validate(responseBody, null);

        //then
        assertThatCode(call).doesNotThrowAnyException();
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenResponseIsMissing() {
        //given
        var jsonNode = mock(JsonNode.class);

        //when
        ThrowableAssert.ThrowingCallable call = () -> responseBodyValidator.validate(null, jsonNode);

        //then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(call)
                .withMessage("Missing transaction status response")
                .satisfies(e -> assertThat(e.getRawResponseBody()).isEqualTo(jsonNode));
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenStatusFieldIsMissingInResponseBody() {
        //given
        var jsonNode = mock(JsonNode.class);

        //when
        ThrowableAssert.ThrowingCallable call = () -> responseBodyValidator.validate(new PaymentServiceProviderDraftPaymentStatusResponse(null), jsonNode);

        //then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(call)
                .withMessage("Missing transaction status")
                .satisfies(e -> assertThat(e.getRawResponseBody()).isEqualTo(jsonNode));
    }

}