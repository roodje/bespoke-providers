package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit;

import com.yolt.providers.common.pis.sepa.SepaPaymentStatus;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatusResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class YoltBankSepaSubmitPaymentIdExtractorTest {

    private YoltBankSepaSubmitPaymentIdExtractor paymentIdExtractor;

    @BeforeEach
    public void setup() {
        paymentIdExtractor = new YoltBankSepaSubmitPaymentIdExtractor();
    }

    @Test
    void shouldReturnProperPaymentId() {
        // given
        SepaPaymentStatusResponse sepaPaymentStatusResponseDTO = new SepaPaymentStatusResponse(
                "fakePaymentId",
                SepaPaymentStatus.INITIATED
        );

        // when
        String result = paymentIdExtractor.extractPaymentId(sepaPaymentStatusResponseDTO, null);

        // then
        assertThat(result).isEqualTo("fakePaymentId");
    }
}
