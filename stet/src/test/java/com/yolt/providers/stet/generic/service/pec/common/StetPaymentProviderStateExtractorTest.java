package com.yolt.providers.stet.generic.service.pec.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.stet.generic.domain.PaymentProviderState;
import com.yolt.providers.stet.generic.dto.payment.response.StetConsentApprovalLink;
import com.yolt.providers.stet.generic.dto.payment.response.StetLinks;
import com.yolt.providers.stet.generic.dto.payment.response.StetPaymentInitiationResponseDTO;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePaymentPaymentIdExtractor;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class StetPaymentProviderStateExtractorTest {

    private static final String PAYMENT_ID = "d4fd6f689";
    private static final String AUTHORIZATION_URL = String.format("https://stetbank.com/payment/%s", PAYMENT_ID);
    private static final String SERIALIZED_PROVIDER_STATE = String.format("{\"paymentId\":\"%s\"}", PAYMENT_ID);

    @Mock
    private StetInitiatePaymentPaymentIdExtractor paymentIdExtractor;

    private StetPaymentProviderStateExtractor<StetPaymentInitiationResponseDTO, StetInitiatePreExecutionResult> providerStateExtractor;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        providerStateExtractor = new StetPaymentProviderStateExtractor<>(paymentIdExtractor, objectMapper);
    }

    @Test
    void shouldExtractPaymentProviderState() {
        // given
        StetPaymentInitiationResponseDTO responseDTO = createStetPaymentInitiationResponseDTO();
        StetInitiatePreExecutionResult preExecutionResult = StetInitiatePreExecutionResult.builder()
                .build();

        given(paymentIdExtractor.extractPaymentId(any(StetPaymentInitiationResponseDTO.class), any(StetInitiatePreExecutionResult.class)))
                .willReturn(PAYMENT_ID);

        // when
        String jsonProviderState = providerStateExtractor.extractProviderState(responseDTO, preExecutionResult);

        // then
        assertThat(jsonProviderState).isEqualTo(SERIALIZED_PROVIDER_STATE);

        then(paymentIdExtractor)
                .should()
                .extractPaymentId(responseDTO, preExecutionResult);
    }

    @Test
    void shouldMapPaymentProviderStateToJson() {
        // when
        PaymentProviderState providerState = providerStateExtractor.mapToPaymentProviderState(SERIALIZED_PROVIDER_STATE);

        // then
        assertThat(providerState.getPaymentId()).isEqualTo(PAYMENT_ID);
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
