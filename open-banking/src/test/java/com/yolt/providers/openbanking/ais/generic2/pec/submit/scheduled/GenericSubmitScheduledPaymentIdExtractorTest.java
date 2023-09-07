package com.yolt.providers.openbanking.ais.generic2.pec.submit.scheduled;

import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledResponse5;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledResponse5Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GenericSubmitScheduledPaymentIdExtractorTest {

    @InjectMocks
    private GenericSubmitScheduledPaymentIdExtractor subject;

    @Test
    void shouldReturnPaymentIdWhenCorrectDataAreProvided() {
        // given
        OBWriteDomesticScheduledResponse5 obWriteDomesticScheduledResponse5 = new OBWriteDomesticScheduledResponse5()
                .data(new OBWriteDomesticScheduledResponse5Data()
                        .domesticScheduledPaymentId("paymentId"));

        // when
        String result = subject.extractPaymentId(obWriteDomesticScheduledResponse5, null);

        // then
        assertThat(result).isEqualTo("paymentId");
    }
}