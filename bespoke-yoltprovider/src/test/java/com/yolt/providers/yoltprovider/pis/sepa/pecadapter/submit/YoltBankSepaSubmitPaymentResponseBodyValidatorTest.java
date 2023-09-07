package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
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
public class YoltBankSepaSubmitPaymentResponseBodyValidatorTest {

    @InjectMocks
    private YoltBankSepaSubmitPaymentResponseBodyValidator responseBodyValidator;

    @Mock
    private JsonNode rawBody;

    @Test
    void shouldNotThrowAnyExceptionWhenResponseBodyIsProvided() {
        // given
        SepaPaymentStatusResponse responseBody = new SepaPaymentStatusResponse(
                "", SepaPaymentStatus.ACCEPTED
        );

        // when
        ThrowableAssert.ThrowingCallable callable = () -> responseBodyValidator.validate(responseBody, rawBody);

        // then
        assertThatCode(callable)
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenResponseBodyIsNull() {
        // when
        ThrowableAssert.ThrowingCallable callable = () -> responseBodyValidator.validate(null, rawBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing response body")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawBody));
    }
}
