package com.yolt.providers.starlingbank.starlingbank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.starlingbank.common.model.PaymentStatusResponse;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.validator.StarlingBankStatusPaymentExecutionContextResponseBodyValidator;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StarlingBankStatusPaymentResponseBodyValidatorTest {

    @InjectMocks
    private StarlingBankStatusPaymentExecutionContextResponseBodyValidator subject;

    @Mock
    private JsonNode rawResponseBody;

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldThrowResponseBodyValidationExceptionForValidateWhenResponseBodyIsNull() {
        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(null, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Response body is missing")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @Test
    void shouldThrowExceptionForMissingPaymentStatusDetailsInResponse() throws TokenInvalidException {
        // given
        PaymentStatusResponse paymentStatusResponse = deserializeFromJson("{\"payments\":[{\"paymentUid\":\"375affe0-3211-4bef-90e7-84c93d790e5a\",\"amount\":{\"currency\":\"PLN\",\"minorUnits\":12311},\"reference\":\"Payment reference\",\"payeeUid\":\"55665566-5566-5566-5566-556655665566\",\"payeeAccountUid\":\"66776677-6677-6677-6677-667766776677\",\"createdAt\":\"2021-02-02T10:43:56.266Z\",\"completedAt\":\"2021-02-02T10:43:56.266Z\",\"rejectedAt\":\"2021-02-02T10:43:56.266Z\"}]}");
        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(paymentStatusResponse, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Missing Payment status details");
    }

    @Test
    void shouldNotThrowAnyExceptionForValidateWhenCorrectData() throws TokenInvalidException {
        // given
        PaymentStatusResponse paymentStatusResponse = deserializeFromJson("{\"payments\":[{\"paymentUid\":\"375affe0-3211-4bef-90e7-84c93d790e5a\",\"amount\":{\"currency\":\"PLN\",\"minorUnits\":12311},\"reference\":\"Payment reference\",\"payeeUid\":\"55665566-5566-5566-5566-556655665566\",\"payeeAccountUid\":\"66776677-6677-6677-6677-667766776677\",\"createdAt\":\"2021-02-02T10:43:56.266Z\",\"completedAt\":\"2021-02-02T10:43:56.266Z\",\"rejectedAt\":\"2021-02-02T10:43:56.266Z\",\"paymentStatusDetails\":{\"paymentStatus\":\"ACCEPTED\",\"description\":\"QUALIFIED_ACCEPT_AFTER_NEXT_WORKING_DAY\"}}]}");
        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(paymentStatusResponse, rawResponseBody);

        // then
        assertThatCode(callable)
                .doesNotThrowAnyException();
    }

    private static PaymentStatusResponse deserializeFromJson(String json) throws TokenInvalidException {
        try {
            return objectMapper.readValue(json, PaymentStatusResponse.class);
        } catch (JsonProcessingException e) {
            throw new TokenInvalidException("Unable to deserialize token");
        }
    }
}
