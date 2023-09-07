package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit.model.PaymentSubmitResponse;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class YoltBankUkDomesticSubmitPaymentResponseBodyValidatorTest {

    @InjectMocks
    private YoltBankUkDomesticSubmitPaymentResponseBodyValidator responseBodyValidator;

    @Mock
    private JsonNode rawBody;

    @Test
    void shouldNotThrowAnyExceptionWhenResponseBodyContainsRequiredProperties() throws Throwable {
        // given
        PaymentSubmitResponse responseBody = new PaymentSubmitResponse();
        PaymentSubmitResponse.Data data = new PaymentSubmitResponse.Data("1234", null, null, null);
        responseBody.setData(data);

        // when
        ThrowableAssert.ThrowingCallable callable = () -> responseBodyValidator.validate(responseBody, rawBody);

        // then
        assertThatCode(callable)
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenResponseBodyIsNull() {
        // given
        PaymentSubmitResponse responseBody = null;

        // when
        ThrowableAssert.ThrowingCallable callable = () -> responseBodyValidator.validate(responseBody, rawBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing response body")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawBody));
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenDataIsMissing() {
        // given
        PaymentSubmitResponse responseBody = new PaymentSubmitResponse();

        // when
        ThrowableAssert.ThrowingCallable callable = () -> responseBodyValidator.validate(responseBody, rawBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing data")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawBody));
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenDomesticPaymentIdIsMissing() throws Throwable {
        // given
        PaymentSubmitResponse responseBody = new PaymentSubmitResponse();
        responseBody.setData(new PaymentSubmitResponse.Data(null, null, null, null));

        // when
        ThrowableAssert.ThrowingCallable callable = () -> responseBodyValidator.validate(responseBody, rawBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing UK domestic payment ID")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawBody));
    }
}
