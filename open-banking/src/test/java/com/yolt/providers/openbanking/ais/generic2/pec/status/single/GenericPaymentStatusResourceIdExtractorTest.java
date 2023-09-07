package com.yolt.providers.openbanking.ais.generic2.pec.status.single;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.PaymentStatusResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GenericPaymentStatusResourceIdExtractorTest {

    @InjectMocks
    private GenericPaymentStatusResourceIdExtractor subject;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private DefaultAuthMeans means;

    @Test
    void shouldReturnDomesticPaymentIdWhenDomesticPaymentIdIsProvidedAsResourceId() {
        // given
        GenericPaymentStatusPreExecutionResult preExecutionResult = new GenericPaymentStatusPreExecutionResult("token", means, restTemplateManager,  "domesticPaymentId", "consentId");

        PaymentStatusResponse paymentStatusResponse = new PaymentStatusResponse();
        paymentStatusResponse.setData(new PaymentStatusResponse.Data("consentId", "domesticPaymentId", PaymentStatusResponse.Data.Status.PENDING));

        // when
        String result = subject.extractPaymentId(paymentStatusResponse, preExecutionResult);

        // then
        assertThat(result).isEqualTo("domesticPaymentId");
    }

    @Test
    void shouldNotReturnConsentIdWhenDomesticPaymentIdIsNotProvidedAsResourceId() {
        // given
        GenericPaymentStatusPreExecutionResult preExecutionResult = new GenericPaymentStatusPreExecutionResult("token", means, restTemplateManager,  "", "consentId");

        PaymentStatusResponse paymentStatusResponse = new PaymentStatusResponse();
        paymentStatusResponse.setData(new PaymentStatusResponse.Data("consentId", "", PaymentStatusResponse.Data.Status.PENDING));

        // when
        String result = subject.extractPaymentId(paymentStatusResponse, preExecutionResult);

        // then
        assertThat(result).isEmpty();
    }
}