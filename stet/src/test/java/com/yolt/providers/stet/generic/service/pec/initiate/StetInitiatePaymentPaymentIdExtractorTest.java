package com.yolt.providers.stet.generic.service.pec.initiate;

import com.yolt.providers.stet.generic.dto.payment.response.StetConsentApprovalLink;
import com.yolt.providers.stet.generic.dto.payment.response.StetLinks;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentInitiationResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StetInitiatePaymentPaymentIdExtractorTest {

    private static final String PAYMENT_ID = "67117df1e2ca460c52084ca261aa85e8";
    private static final String AUTHORIZATION_URL = String.format("https://stetbank.com/payment/?paymentRequestResourceId=%s", PAYMENT_ID);

    private StetInitiatePaymentPaymentIdExtractor initiatePaymentIdExtractor;

    @BeforeEach
    void initialize() {
        initiatePaymentIdExtractor = new StetInitiatePaymentPaymentIdExtractor();
    }

    @Test
    void shouldExtractPaymentId() {
        // given
        StetPaymentInitiationResponseDTO responseDTO = createStetPaymentInitiationResponseDTO();

        // when
        String paymentId = initiatePaymentIdExtractor.extractPaymentId(responseDTO, null);

        // then
        assertThat(paymentId).isEqualTo(PAYMENT_ID);
    }

    private StetPaymentInitiationResponseDTO createStetPaymentInitiationResponseDTO() {
        StetConsentApprovalLink consentApprovalLink = new StetConsentApprovalLink();
        consentApprovalLink.setHref(AUTHORIZATION_URL);

        StetLinks links = new StetLinks();
        links.setConsentApproval(consentApprovalLink);

        StetPaymentInitiationResponseDTO responseDTO = new StetPaymentInitiationResponseDTO();
        responseDTO.setLinks(links);
        return responseDTO;
    }
}
