package com.yolt.providers.stet.generic.service.pec.initiate;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class StetInitiatePaymentResponseBodyValidatorTest {

    private static final String PAYMENT_ID = "67117df1e2ca460c52084ca261aa85e8";
    private static final String BASE_URL = "https://stetbank.com/payment/";
    private static final String AUTHORIZATION_URL = String.format("%s?paymentRequestResourceId=%s", BASE_URL, PAYMENT_ID);

    @Mock
    private StetInitiatePaymentPaymentIdExtractor paymentIdExtractor;

    private StetInitiatePaymentResponseBodyValidator initiatePaymentResponseBodyValidator;

    @BeforeEach
    void initialize() {
        initiatePaymentResponseBodyValidator = new StetInitiatePaymentResponseBodyValidator(paymentIdExtractor);
    }

    @Test
    void shouldSuccessfullyValidateResponseBody() throws ResponseBodyValidationException {
        // given
        StetPaymentInitiationResponseDTO responseDTO = createStetPaymentInitiationResponseDTO(AUTHORIZATION_URL);
        JsonNode rawResponseBody = JsonNodeFactory.instance.textNode("Payment Initiation");

        given(paymentIdExtractor.getPaymentIdQueryParameterSupplier())
                .willReturn(() -> "paymentRequestResourceId");

        // when
        initiatePaymentResponseBodyValidator.validate(responseDTO, rawResponseBody);

        // then
        then(paymentIdExtractor)
                .should()
                .getPaymentIdQueryParameterSupplier();
    }

    private static Stream<Arguments> provideAuthorizationUrlAndExpectedErrorMessage() {
        return Stream.of(
                Arguments.of(null, "Authorization URL is missing"),
                Arguments.of("", "Authorization URL is missing"),
                Arguments.of(" ", "Authorization URL is missing"));
    }

    @MethodSource("provideAuthorizationUrlAndExpectedErrorMessage")
    @ParameterizedTest
    void shouldThrowResponseBodyValidationExceptionDueToValidationViolation(String givenAuthorizationUrl, String expectedErrorMessage) {
        // given
        StetPaymentInitiationResponseDTO responseDTO = createStetPaymentInitiationResponseDTO(givenAuthorizationUrl);
        JsonNode rawResponseBody = JsonNodeFactory.instance.textNode("Payment Initiation");

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () ->
                initiatePaymentResponseBodyValidator.validate(responseDTO, rawResponseBody);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(ResponseBodyValidationException.class)
                .hasMessage(expectedErrorMessage)
                .satisfies(exception -> {
                    assertThat(((ResponseBodyValidationException) exception).getRawResponseBody()).isEqualTo(rawResponseBody);
                });
    }

    @Test
    void shouldThrowResponseBodyValidationExceptionDueToMissingPaymentId() {
        // given
        StetPaymentInitiationResponseDTO responseDTO = createStetPaymentInitiationResponseDTO(BASE_URL);
        JsonNode rawResponseBody = JsonNodeFactory.instance.textNode("Payment Initiation");

        given(paymentIdExtractor.getPaymentIdQueryParameterSupplier())
                .willReturn(() -> "paymentRequestResourceId");

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () ->
                initiatePaymentResponseBodyValidator.validate(responseDTO, rawResponseBody);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(ResponseBodyValidationException.class)
                .hasMessage("Payment ID is missing")
                .satisfies(exception -> {
                    assertThat(((ResponseBodyValidationException) exception).getRawResponseBody()).isEqualTo(rawResponseBody);
                });
    }

    private StetPaymentInitiationResponseDTO createStetPaymentInitiationResponseDTO(String authorizeUrl) {
        StetConsentApprovalLink consentApprovalLink = new StetConsentApprovalLink();
        consentApprovalLink.setHref(authorizeUrl);

        StetLinks links = new StetLinks();
        links.setConsentApproval(consentApprovalLink);

        StetPaymentInitiationResponseDTO responseDTO = new StetPaymentInitiationResponseDTO();
        responseDTO.setLinks(links);
        return responseDTO;
    }
}
