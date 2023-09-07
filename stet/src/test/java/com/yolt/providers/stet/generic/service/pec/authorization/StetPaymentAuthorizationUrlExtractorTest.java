package com.yolt.providers.stet.generic.service.pec.authorization;

import com.yolt.providers.stet.generic.dto.payment.response.StetConsentApprovalLink;
import com.yolt.providers.stet.generic.dto.payment.response.StetLinks;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentInitiationResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StetPaymentAuthorizationUrlExtractorTest {

    private static final String AUTHORIZATION_URL = "https://stetbank.com/payment/67117df1e2ca460c52084ca261aa85e8";

    private StetPaymentAuthorizationUrlExtractor authorizationUrlExtractor;

    @BeforeEach
    void setUp() {
        authorizationUrlExtractor = new StetPaymentAuthorizationUrlExtractor();
    }

    @Test
    void shouldExtractAuthorizationUrl() {
        // given
        StetPaymentInitiationResponseDTO responseDTO = createStetPaymentInitiationResponseDTO();

        // when
        String authorizationUrl = authorizationUrlExtractor.extractAuthorizationUrl(responseDTO, null);

        // then
        assertThat(authorizationUrl).isEqualTo(AUTHORIZATION_URL);
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
