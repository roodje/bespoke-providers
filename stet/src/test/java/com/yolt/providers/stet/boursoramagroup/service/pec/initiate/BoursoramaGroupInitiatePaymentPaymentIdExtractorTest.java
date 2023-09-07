package com.yolt.providers.stet.boursoramagroup.service.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentAuthorizationUrlExtractor;
import com.yolt.providers.stet.boursoramagroup.common.service.pec.initiate.BoursoramaGroupInitiatePaymentPaymentIdExtractor;
import com.yolt.providers.stet.generic.dto.payment.response.StetConsentApprovalLink;
import com.yolt.providers.stet.generic.dto.payment.response.StetLinks;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentInitiationResponseDTO;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BoursoramaGroupInitiatePaymentPaymentIdExtractorTest {

    @Mock
    private PaymentAuthorizationUrlExtractor<StetPaymentInitiationResponseDTO, StetInitiatePreExecutionResult> authorizationUrlExtractor;

    private BoursoramaGroupInitiatePaymentPaymentIdExtractor initiatePaymentPaymentIdExtractor;

    @BeforeEach
    void initialize() {
        initiatePaymentPaymentIdExtractor = new BoursoramaGroupInitiatePaymentPaymentIdExtractor(authorizationUrlExtractor);
    }

    @Test
    void shouldExtractPaymentIdFromQueryParameter() {
        // given
        String consentApprovalUrl = "https://clients.boursorama.com/feature-redirect?featureId=customer.accounts_cashtransfer_prebuilt_transfer_form&params%5BresourceId%5D=83f9730518ee4bdfa1b024d1642c83c2";
        StetPaymentInitiationResponseDTO stetPaymentInitiationResponseDTO = createStetPaymentInitiationResponseDTO(consentApprovalUrl);
        StetInitiatePreExecutionResult stetInitiatePreExecutionResult = createStetInitiatePreExecutionResult();

        when(authorizationUrlExtractor.extractAuthorizationUrl(eq(stetPaymentInitiationResponseDTO), eq(stetInitiatePreExecutionResult)))
                .thenReturn(consentApprovalUrl);

        // when
        String paymentId = initiatePaymentPaymentIdExtractor.extractPaymentId(stetPaymentInitiationResponseDTO, stetInitiatePreExecutionResult);

        // then
        assertThat(paymentId).isEqualTo("83f9730518ee4bdfa1b024d1642c83c2");
    }

    @Test
    void shouldExtractPaymentIdFromPathSegment() {
        // given
        String consentApprovalUrl = "https://clients.boursorama.com/finalisation-virement/dfa1b024d1642c83c2";
        StetPaymentInitiationResponseDTO stetPaymentInitiationResponseDTO = createStetPaymentInitiationResponseDTO(consentApprovalUrl);
        StetInitiatePreExecutionResult stetInitiatePreExecutionResult = createStetInitiatePreExecutionResult();

        when(authorizationUrlExtractor.extractAuthorizationUrl(eq(stetPaymentInitiationResponseDTO), eq(stetInitiatePreExecutionResult)))
                .thenReturn(consentApprovalUrl);

        // when
        String paymentId = initiatePaymentPaymentIdExtractor.extractPaymentId(stetPaymentInitiationResponseDTO, stetInitiatePreExecutionResult);

        // then
        assertThat(paymentId).isEqualTo("dfa1b024d1642c83c2");
    }

    @Test
    void shouldReturnNullWhenPaymentIdIsNotProvidedInQueryParameterAndPathSegment() {
        // given
        String consentApprovalUrl = "https:\\/clients.boursorama.com\\/finalisation-virement\\/dfa1b024d1642c83c2";
        StetPaymentInitiationResponseDTO stetPaymentInitiationResponseDTO = createStetPaymentInitiationResponseDTO(consentApprovalUrl);
        StetInitiatePreExecutionResult stetInitiatePreExecutionResult = createStetInitiatePreExecutionResult();

        when(authorizationUrlExtractor.extractAuthorizationUrl(eq(stetPaymentInitiationResponseDTO), eq(stetInitiatePreExecutionResult)))
                .thenReturn(consentApprovalUrl);

        // when
        String paymentId = initiatePaymentPaymentIdExtractor.extractPaymentId(stetPaymentInitiationResponseDTO, stetInitiatePreExecutionResult);

        // then
        assertThat(paymentId).isNull();
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

    private StetInitiatePreExecutionResult createStetInitiatePreExecutionResult() {
        return StetInitiatePreExecutionResult.builder()
                .build();
    }
}
