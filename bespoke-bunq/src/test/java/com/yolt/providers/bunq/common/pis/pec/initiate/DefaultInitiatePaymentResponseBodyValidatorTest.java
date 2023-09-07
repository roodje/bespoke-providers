package com.yolt.providers.bunq.common.pis.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.bunq.common.model.IdObject;
import com.yolt.providers.bunq.common.model.IdResponse;
import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentResponse;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DefaultInitiatePaymentResponseBodyValidatorTest {

    private DefaultInitiatePaymentResponseBodyValidator responseBodyValidator = new DefaultInitiatePaymentResponseBodyValidator();

    @Test
    void shouldNotThrowAnyExceptionWhenCorrectDataAreProvided() {
        //given
        List<IdResponse> idResponseList = List.of(new IdResponse(new IdObject(12345)));
        var responseBody = new PaymentServiceProviderDraftPaymentResponse(idResponseList);

        //when
        ThrowableAssert.ThrowingCallable call = () -> responseBodyValidator.validate(responseBody, null);

        //then
        assertThatCode(call).doesNotThrowAnyException();
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenPaymentIdIsMissingInResponseBody() {
        //given
        var jsonNode = mock(JsonNode.class);

        //when
        ThrowableAssert.ThrowingCallable call = () -> responseBodyValidator.validate(new PaymentServiceProviderDraftPaymentResponse(null), jsonNode);

        //then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(call)
                .withMessage("Response body doesn't contain paymentId")
                .satisfies(e -> assertThat(e.getRawResponseBody()).isEqualTo(jsonNode));
    }

}