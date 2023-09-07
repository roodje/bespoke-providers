package com.yolt.providers.openbanking.ais.generic2.sepa.mapper;

import com.yolt.providers.common.pis.common.PaymentStatusResponseDTO;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionContextMetadata;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatusResponseDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SepaSubmitPaymentResponseMapperTest {

    private SepaSubmitPaymentResponseMapper subject = new SepaSubmitPaymentResponseMapper();

    @Test
    void shouldMap() {
        //Given
        var contextMetadata = new PaymentExecutionContextMetadata(null, null, null, null, null, null, null);
        var ukResponse = new PaymentStatusResponseDTO("providerState", "paymentId", contextMetadata);
        var expectedResult = new SepaPaymentStatusResponseDTO("providerState", "paymentId", contextMetadata);

        //When
        var result = subject.map(ukResponse);

        //Then
        assertThat(result).usingRecursiveComparison().isEqualTo(expectedResult);
    }
}