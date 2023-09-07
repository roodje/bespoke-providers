package com.yolt.providers.knabgroup.common.payment.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.knabgroup.common.payment.dto.external.StatusPaymentResponse;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DefaultStatusPaymentResponseBodyValidatorTest {

    @InjectMocks
    private DefaultStatusPaymentResponseBodyValidator subject;

    @Mock
    private JsonNode rawResponseBody;

    @Test
    void shouldThrowResponseBodyValidationExceptionForValidateWhenResponseBodyIsNull() {
        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(null, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing transaction status response")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @ParameterizedTest
    @MethodSource("invalidStatusResponses")
    void shouldThrowResponseBodyValidationExceptionForValidateWhenTransactionStatusIsNull(final StatusPaymentResponse response) {
        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(response, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing transaction status")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @Test
    void shouldNotThrowAnyExceptionForValidateWhenCorrectData() {
        // given
        StatusPaymentResponse sampleResponse = new StatusPaymentResponse("transactinStatus");

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(sampleResponse, rawResponseBody);

        // then
        assertThatCode(callable)
                .doesNotThrowAnyException();
    }

    private static List<StatusPaymentResponse> invalidStatusResponses() {
        return List.of(
                new StatusPaymentResponse(null),
                new StatusPaymentResponse("")
        );
    }
}
