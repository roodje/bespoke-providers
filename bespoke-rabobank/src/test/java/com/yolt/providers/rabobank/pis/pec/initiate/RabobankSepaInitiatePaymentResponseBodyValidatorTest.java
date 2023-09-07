package com.yolt.providers.rabobank.pis.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.rabobank.dto.external.HrefType;
import com.yolt.providers.rabobank.dto.external.InitiatedTransactionResponse;
import com.yolt.providers.rabobank.dto.external.Links;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabobankSepaInitiatePaymentResponseBodyValidatorTest {

    private RabobankSepaInitiatePaymentResponseBodyValidator subject;

    @BeforeEach
    void setUp() {
        subject = new RabobankSepaInitiatePaymentResponseBodyValidator();
    }

    @Test
    void shouldValidateCorrectResponse() throws ResponseBodyValidationException {
        //given
        HrefType hrefType = mock(HrefType.class);
        when(hrefType.getHref()).thenReturn("http://imfancyscaredirect.com/clickheretoconfirmpayment");
        Links links = mock(Links.class);
        when(links.getScaRedirect()).thenReturn(hrefType);
        InitiatedTransactionResponse initiatedTransactionResponse = mock(InitiatedTransactionResponse.class);
        when(initiatedTransactionResponse.getLinks()).thenReturn(links);

        //when
        ThrowableAssert.ThrowingCallable call = () -> subject.validate(initiatedTransactionResponse, null);

        //then
        assertThatCode(call).doesNotThrowAnyException();
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWhenReceivedInvalidResponse() {
        //given
        InitiatedTransactionResponse initiatedTransactionResponse = mock(InitiatedTransactionResponse.class);
        when(initiatedTransactionResponse.getLinks()).thenReturn(null);
        JsonNode rawResponseBody = mock(JsonNode.class);

        //when
        ThrowableAssert.ThrowingCallable call = () -> subject.validate(initiatedTransactionResponse, rawResponseBody);

        //then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(call)
                .withMessage("Missing SCA redirect link")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }
}
