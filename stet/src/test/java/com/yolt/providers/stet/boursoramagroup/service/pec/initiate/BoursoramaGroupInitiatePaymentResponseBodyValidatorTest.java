package com.yolt.providers.stet.boursoramagroup.service.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.ResponseBodyValidationException;
import com.yolt.providers.stet.boursoramagroup.common.service.pec.initiate.BoursoramaGroupInitiatePaymentResponseBodyValidator;
import com.yolt.providers.stet.generic.dto.payment.response.StetConsentApprovalLink;
import com.yolt.providers.stet.generic.dto.payment.response.StetLinks;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentInitiationResponseDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BoursoramaGroupInitiatePaymentResponseBodyValidatorTest {

    private static final JsonNode JSON_NODE = JsonNodeFactory.instance.textNode("Anything");

    private BoursoramaGroupInitiatePaymentResponseBodyValidator initiatePaymentResponseBodyValidator;

    @BeforeEach
    void initialize() {
        initiatePaymentResponseBodyValidator = new BoursoramaGroupInitiatePaymentResponseBodyValidator();
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionDueToMissingPaymentId() {
        // given
        String consentApprovalUrl = "https://clients.boursorama.com/finalisation-virement";
        StetPaymentInitiationResponseDTO stetPaymentInitiationResponseDTO = createStetPaymentInitiationResponseDTO(consentApprovalUrl);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () ->
                initiatePaymentResponseBodyValidator.validate(stetPaymentInitiationResponseDTO, JSON_NODE);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(ResponseBodyValidationException.class)
                .hasMessage("Payment ID is missing")
                .satisfies(exception -> {
                    assertThat(((ResponseBodyValidationException) exception).getRawResponseBody()).isEqualTo(JSON_NODE);
                });
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionDueToMissingAuthorizationUrl() {
        // given
        String consentApprovalUrl = "https://clients.boursorama.com/feature-redirect?params%5BresourceId%5D";
        StetPaymentInitiationResponseDTO stetPaymentInitiationResponseDTO = createStetPaymentInitiationResponseDTO(consentApprovalUrl);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () ->
                initiatePaymentResponseBodyValidator.validate(stetPaymentInitiationResponseDTO, JSON_NODE);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(ResponseBodyValidationException.class)
                .hasMessage("Payment ID is missing")
                .satisfies(exception -> {
                    assertThat(((ResponseBodyValidationException) exception).getRawResponseBody()).isEqualTo(JSON_NODE);
                });
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionDueToBlankPaymentIdInParameter() {
        // given
        String consentApprovalUrl = "";
        StetPaymentInitiationResponseDTO stetPaymentInitiationResponseDTO = createStetPaymentInitiationResponseDTO(consentApprovalUrl);

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () ->
                initiatePaymentResponseBodyValidator.validate(stetPaymentInitiationResponseDTO, JSON_NODE);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(ResponseBodyValidationException.class)
                .hasMessage("Authorization URL is missing")
                .satisfies(exception -> {
                    assertThat(((ResponseBodyValidationException) exception).getRawResponseBody()).isEqualTo(JSON_NODE);
                });
    }

    @Test
    void shouldPassValidationWithPaymentIdFoundInQueryParameter() throws ResponseBodyValidationException {
        // given
        String consentApprovalUrl = "https://clients.boursorama.com/feature-redirect?params%5BresourceId%5D=34271231";
        StetPaymentInitiationResponseDTO stetPaymentInitiationResponseDTO = createStetPaymentInitiationResponseDTO(consentApprovalUrl);

        // when-then
        initiatePaymentResponseBodyValidator.validate(stetPaymentInitiationResponseDTO, JSON_NODE);
    }

    @Test
    void shouldPassValidationWithPaymentIdFoundInSegmentPath() throws ResponseBodyValidationException {
        // given
        String consentApprovalUrl = "https://clients.boursorama.com/finalisation-virement/34271231";
        StetPaymentInitiationResponseDTO stetPaymentInitiationResponseDTO = createStetPaymentInitiationResponseDTO(consentApprovalUrl);

        // when-then
        initiatePaymentResponseBodyValidator.validate(stetPaymentInitiationResponseDTO, JSON_NODE);
    }

    private StetPaymentInitiationResponseDTO createStetPaymentInitiationResponseDTO(String consentApprovalUrl) {
        StetConsentApprovalLink consentApproval = new StetConsentApprovalLink();
        consentApproval.setHref(consentApprovalUrl);

        StetLinks links = new StetLinks();
        links.setConsentApproval(consentApproval);

        StetPaymentInitiationResponseDTO responseDTO = new StetPaymentInitiationResponseDTO();
        responseDTO.setLinks(links);
        return responseDTO;
    }
}
