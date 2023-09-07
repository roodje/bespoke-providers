package com.yolt.providers.stet.generic.service.pec.confirmation.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentStatus;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentRequest;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentStatusResponseDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class StetStatusResponseBodyValidatorTest {

    private StetStatusResponseBodyValidator statusResponseBodyValidator;

    @BeforeEach
    void initialize() {
        statusResponseBodyValidator = new StetStatusResponseBodyValidator();
    }

    @Test
    void shouldSuccessfullyValidateResponseBody() throws ResponseBodyValidationException {
        // given
        StetPaymentStatusResponseDTO responseDTO = createStetPaymentStatusResponseDTO(StetPaymentStatus.ACCP);
        JsonNode rawResponseBody = JsonNodeFactory.instance.textNode("Payment Initiation");

        // when-then
        statusResponseBodyValidator.validate(responseDTO, rawResponseBody);
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionDueToValidationViolation() {
        // given
        StetPaymentStatusResponseDTO responseDTO = createStetPaymentStatusResponseDTO(null);
        JsonNode rawResponseBody = JsonNodeFactory.instance.textNode("Payment Initiation");

        // when
        ThrowableAssert.ThrowingCallable throwingCallable =
                () -> statusResponseBodyValidator.validate(responseDTO, rawResponseBody);

        assertThatThrownBy(throwingCallable).isInstanceOf(ResponseBodyValidationException.class)
                .hasMessage("Payment status is missing")
                .satisfies(exception -> {
                    assertThat(((ResponseBodyValidationException) exception).getRawResponseBody()).isEqualTo(rawResponseBody);
                });
    }

    private StetPaymentStatusResponseDTO createStetPaymentStatusResponseDTO(StetPaymentStatus stetPaymentStatus) {
        StetPaymentRequest stetPaymentRequest = new StetPaymentRequest();
        stetPaymentRequest.setPaymentInformationStatus(stetPaymentStatus);

        StetPaymentStatusResponseDTO responseDTO = new StetPaymentStatusResponseDTO();
        responseDTO.setPaymentRequest(stetPaymentRequest);
        return responseDTO;
    }
}
