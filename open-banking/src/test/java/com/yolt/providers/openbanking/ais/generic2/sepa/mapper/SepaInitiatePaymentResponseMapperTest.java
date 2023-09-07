package com.yolt.providers.openbanking.ais.generic2.sepa.mapper;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionContextMetadata;
import com.yolt.providers.common.pis.sepa.LoginUrlAndStateDTO;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentResponseDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SepaInitiatePaymentResponseMapperTest {

    private SepaInitiatePaymentResponseMapper subject = new SepaInitiatePaymentResponseMapper();

    @Test
    void shouldMap() {
        //Given
        var contextMetadata = new PaymentExecutionContextMetadata(null, null, null, null, null, null, null);
        var ukResponse = new InitiateUkDomesticPaymentResponseDTO("loginUrl", "providerState", contextMetadata);
        var expectedResult = new LoginUrlAndStateDTO("loginUrl", "providerState", contextMetadata);

        //When
        var result = subject.map(ukResponse);

        //Then
        assertThat(result).usingRecursiveComparison().isEqualTo(expectedResult);
    }

}