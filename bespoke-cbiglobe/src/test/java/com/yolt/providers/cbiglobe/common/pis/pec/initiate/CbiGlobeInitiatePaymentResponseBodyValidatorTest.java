package com.yolt.providers.cbiglobe.common.pis.pec.initiate;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.yolt.providers.cbiglobe.pis.dto.LinksPaymentinitiationrequestType1;
import com.yolt.providers.cbiglobe.pis.dto.PaymentInitiationRequestResponseType;
import com.yolt.providers.cbiglobe.pis.dto.ScaRedirectLinkType;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CbiGlobeInitiatePaymentResponseBodyValidatorTest {

    @InjectMocks
    private CbiGlobeInitiatePaymentResponseBodyValidator subject;

    @Test
    void shouldThrowResponseBodyValidationExceptionWithProperMessageForValidateWhenPaymentIdIsMissingInResponse() {
        // given
        var initiatePaymentResponse = new PaymentInitiationRequestResponseType();
        var rawResponseBody = JsonNodeFactory.instance.textNode("");

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(initiatePaymentResponse, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Payment ID is missing")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWithProperMessageForValidateWhenInitiatePaymentResponseIsMissing() {
        // given
        var rawResponseBody = JsonNodeFactory.instance.textNode("");

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(null, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("InitiatePaymentResponse is empty")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionWithProperMessageForValidateWhenRedirectUrlHrefIsMissingInResponse() {
        // given
        var initiatePaymentResponse = new PaymentInitiationRequestResponseType();
        initiatePaymentResponse.setPaymentId("fakePaymentId");
        var rawResponseBody = JsonNodeFactory.instance.textNode("");

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(initiatePaymentResponse, rawResponseBody);

        // then
        assertThatExceptionOfType(ResponseBodyValidationException.class)
                .isThrownBy(callable)
                .withMessage("Authorization redirect URL is missing")
                .satisfies(ex -> assertThat(ex.getRawResponseBody()).isEqualTo(rawResponseBody));
    }

    @Test
    void shouldNotThrowAnyExceptionForValidateWhenPaymentIdInResponse() {
        // given
        var initiatePaymentResponse = new PaymentInitiationRequestResponseType();
        initiatePaymentResponse.setPaymentId("fakePaymentId");
        ScaRedirectLinkType redirect = new ScaRedirectLinkType();
        redirect.setHref("fakeRedirectHref");
        LinksPaymentinitiationrequestType1 links = new LinksPaymentinitiationrequestType1();
        links.setScaRedirect(redirect);
        initiatePaymentResponse.setLinks(links);
        var rawResponseBody = JsonNodeFactory.instance.textNode("");

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.validate(initiatePaymentResponse, rawResponseBody);

        // then
        assertThatCode(callable)
                .doesNotThrowAnyException();
    }
}