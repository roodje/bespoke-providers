package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.yoltprovider.pis.ukdomestic.InitiatePaymentConsentResponse;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class YoltBankUkDomesticPaymentResponseBodyValidatorTest {

    @InjectMocks
    private YoltBankUkDomesticPaymentResponseBodyValidator responseBodyValidator;

    @Mock
    private JsonNode rawBody;

    @Test
    void shouldNotThrowAnyExceptionWhenResponseBodyContainsRequiredProperties() {
        // given
        InitiatePaymentConsentResponse responseBody = new InitiatePaymentConsentResponse(
                "uri",
                "paymentConsent"
        );

        // when
        ThrowableAssert.ThrowingCallable callable = () -> responseBodyValidator.validate(responseBody, rawBody);

        // then
        assertThatCode(callable)
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenResponseBodyIsNull() {
        // given
        InitiatePaymentConsentResponse responseBody = null;

        // when
        ThrowableAssert.ThrowingCallable callable = () -> responseBodyValidator.validate(responseBody, rawBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing response body")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawBody));
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenConsentUriIsMissing() {
        // given
        InitiatePaymentConsentResponse responseBody = new InitiatePaymentConsentResponse(
                "", ""
        );

        // when
        ThrowableAssert.ThrowingCallable callable = () -> responseBodyValidator.validate(responseBody, rawBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing consent URI")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawBody));
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenPaymentConsentIsMissing() {
        // given
        InitiatePaymentConsentResponse responseBody = new InitiatePaymentConsentResponse(
                "uri", ""
        );

        // when
        ThrowableAssert.ThrowingCallable callable = () -> responseBodyValidator.validate(responseBody, rawBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing payment consent")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawBody));
    }
}
