package com.yolt.providers.stet.societegeneralegroup.common.service.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.stet.generic.dto.payment.response.StetConsentApprovalLink;
import com.yolt.providers.stet.generic.dto.payment.response.StetLinks;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentInitiationResponseDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SocieteGeneraleInitiatePaymentResponseBodyValidatorTest {

    @Mock
    private SocieteGeneraleGroupInitiatePaymentIdExtractor paymentIdExtractor;

    private SocieteGeneraleInitiatePaymentResponseBodyValidator responseBodyValidator;

    @BeforeEach
    void setUp() {
        responseBodyValidator = new SocieteGeneraleInitiatePaymentResponseBodyValidator(paymentIdExtractor);
    }

    @Test
    void shouldNotThrowAnyExceptionWhenCorrectDataAreReturnedFromBank() {
        //given
        StetPaymentInitiationResponseDTO responseDTO = preparePaymentInitiationResponse("some-href");
        given(paymentIdExtractor.extractPaymentId(responseDTO, null)).willReturn("some-payment-id");

        //when
        ThrowableAssert.ThrowingCallable call = () -> responseBodyValidator.validate(responseDTO, null);

        //then
        assertThatCode(call).doesNotThrowAnyException();
    }

    @Test
    void shouldThrowResponseBdyValidationExceptionWhenAuthorizationUrlIsMissing() {
        //given
        StetPaymentInitiationResponseDTO responseDTO = preparePaymentInitiationResponse(null);
        JsonNode rawResponseBody = JsonNodeFactory.instance.textNode("some-raw-response");

        //when
        ThrowableAssert.ThrowingCallable call = () -> responseBodyValidator.validate(responseDTO, rawResponseBody);

        //then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(call)
                .withMessage("Authorization URL is missing")
                .hasFieldOrPropertyWithValue("rawResponseBody", rawResponseBody);
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionPaymentIdIsMissing() {
        //given
        StetPaymentInitiationResponseDTO responseDTO = preparePaymentInitiationResponse("some-href");
        given(paymentIdExtractor.extractPaymentId(responseDTO, null)).willReturn(null);
        JsonNode rawResponseBody = JsonNodeFactory.instance.textNode("some-raw-response");

        //when
        ThrowableAssert.ThrowingCallable call = () -> responseBodyValidator.validate(responseDTO, rawResponseBody);

        //then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(call)
                .withMessage("Payment ID is missing")
                .hasFieldOrPropertyWithValue("rawResponseBody", rawResponseBody);
    }

    private StetPaymentInitiationResponseDTO preparePaymentInitiationResponse(String href) {
        StetConsentApprovalLink consentApprovalLink = new StetConsentApprovalLink();
        consentApprovalLink.setHref(href);
        StetLinks links = new StetLinks();
        links.setConsentApproval(consentApprovalLink);
        StetPaymentInitiationResponseDTO paymentInitiationResponseDTO = new StetPaymentInitiationResponseDTO();
        paymentInitiationResponseDTO.setLinks(links);
        return paymentInitiationResponseDTO;
    }
}