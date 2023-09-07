package com.yolt.providers.knabgroup.common.payment.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.knabgroup.common.payment.dto.external.InitiatePaymentResponse;
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
class DefaultInitiatePaymentResponseBodyValidatorTest {

    @InjectMocks
    private DefaultInitiatePaymentResponseBodyValidator subject;

    @Mock
    private JsonNode rawResponseBody;

    @Test
    void shouldThrowResponseBodyValidationExceptionForValidateWhenInitiatePaymentResponseIsNull() {
        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(null, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing response initiation object")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionForValidateWhenTransactionIdIsNull() {
        //given
        InitiatePaymentResponse sampleResponse = new InitiatePaymentResponse(null, "transactinStatus", "redirectUrl");

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(sampleResponse, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing transaction ID")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @ParameterizedTest
    @MethodSource("invalidStatusResponses")
    void shouldThrowResponseBodyValidationExceptionForValidateWhenTransactionStatusIsNull(final InitiatePaymentResponse response) {
        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(response, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing transaction status")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @ParameterizedTest
    @MethodSource("invalidScaResponses")
    void shouldThrowResponseBodyValidationExceptionForValidateWhenScaRedirectIsMissing(final InitiatePaymentResponse response) {
        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(response, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing sca redirect url")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @Test
    void shouldNotThrowAnyExceptionForValidateWhenCorrectData() {
        // given
        InitiatePaymentResponse sampleResponse = new InitiatePaymentResponse("paymentId", "transactinStatus", "redirectUrl");

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(sampleResponse, rawResponseBody);

        // then
        assertThatCode(callable)
                .doesNotThrowAnyException();
    }

    private static List<InitiatePaymentResponse> invalidStatusResponses() {
        return List.of(
                new InitiatePaymentResponse("transactionId", null, "redirectUrl"),
                new InitiatePaymentResponse("transactionId", "", "redirectUrl")
        );
    }

    private static List<InitiatePaymentResponse> invalidScaResponses() {
        return List.of(
                new InitiatePaymentResponse("transactionId", "RCVD", null),
                new InitiatePaymentResponse("transactionId", "RCVD", "")
        );
    }
}