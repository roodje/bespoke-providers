package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentResponseDTO;
import com.yolt.providers.common.pis.sepa.SepaLinksDTO;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatus;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatusResponseDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class YoltBankSepaInitiatePaymentResponseBodyValidatorTest {

    @InjectMocks
    private YoltBankSepaInitiatePaymentResponseBodyValidator paymentResponseBodyValidator;

    @Mock
    private JsonNode rawBody;

    @Test
    void shouldNotThrowAnyExceptionWhenResponseBodyWithRequiredFieldsProvided() {
        // given
        SepaInitiatePaymentResponse responseBody = new SepaInitiatePaymentResponse(
                "sca",
                "123",
                SepaPaymentStatus.ACCEPTED
        );

        // when
        ThrowableAssert.ThrowingCallable callable = () -> paymentResponseBodyValidator.validate(responseBody, rawBody);

        // then
        assertThatCode(callable)
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenResponseBodyNull() {
        // when
        ThrowableAssert.ThrowingCallable callable = () -> paymentResponseBodyValidator.validate(null, rawBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing response body")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawBody));
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenPaymentStatusIsNull() {
        // given
        SepaInitiatePaymentResponse responseBody = new SepaInitiatePaymentResponse(
               "sca",
                "",
                null
        );

        // when
        ThrowableAssert.ThrowingCallable callable = () -> paymentResponseBodyValidator.validate(responseBody, rawBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing Payment Status")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawBody));
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenScaRedirectIsMissing() {
        // given
        SepaInitiatePaymentResponse responseBody = new SepaInitiatePaymentResponse(
                "",
                "123",
                SepaPaymentStatus.INITIATED
        );

        // when
        ThrowableAssert.ThrowingCallable callable = () -> paymentResponseBodyValidator.validate(responseBody, rawBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing SCA redirect link")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawBody));
    }
}
