package com.yolt.providers.ing.common.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.common.pis.sepa.SepaLinksDTO;
import com.yolt.providers.ing.common.dto.InitiatePaymentResponse;
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
    private DefaultInitiatePaymentResponseBodyValidator sut;

    @Mock
    private JsonNode rawResponseBody;

    @Test
    void shouldThrowResponseBodyValidationExceptionForValidateWhenInitiatePaymentResponseIsNull() {
        // when
        ThrowableAssert.ThrowingCallable callable = () -> sut.validate(null, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing response initiation object")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionForValidateWhenTransactionIdIsNull() {
        // when
        ThrowableAssert.ThrowingCallable callable = () -> sut.validate(new InitiatePaymentResponse(), rawResponseBody);

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
        ThrowableAssert.ThrowingCallable callable = () -> sut.validate(response, rawResponseBody);

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
        ThrowableAssert.ThrowingCallable callable = () -> sut.validate(response, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing sca redirect url")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @Test
    void shouldNotThrowAnyExceptionForValidateWhenCorrectData() {
        // given
        InitiatePaymentResponse initiatePaymentResponse = new InitiatePaymentResponse(
                "RCVD",
                "transactionId",
                null,
                new SepaLinksDTO("scaRedirect", "")
        );

        // when
        ThrowableAssert.ThrowingCallable callable = () -> sut.validate(initiatePaymentResponse, rawResponseBody);

        // then
        assertThatCode(callable)
                .doesNotThrowAnyException();
    }

    private static List<InitiatePaymentResponse> invalidStatusResponses() {
        return List.of(
                new InitiatePaymentResponse(
                        null,
                        "transactionId",
                        null,
                        new SepaLinksDTO("scaRedirect", "")
                ),
                new InitiatePaymentResponse(
                        "",
                        "transactionId",
                        null,
                        new SepaLinksDTO("scaRedirect", "")
                )
        );
    }

    private static List<InitiatePaymentResponse> invalidScaResponses() {
        return List.of(
                new InitiatePaymentResponse(
                        "RCVD",
                        "transactionId",
                        null,
                        null
                ),
                new InitiatePaymentResponse(
                        "RCVD",
                        "transactionId",
                        null,
                        new SepaLinksDTO(null, "")
                ),
                new InitiatePaymentResponse(
                        "RCVD",
                        "transactionId",
                        null,
                        new SepaLinksDTO("", "")
                )
        );
    }
}