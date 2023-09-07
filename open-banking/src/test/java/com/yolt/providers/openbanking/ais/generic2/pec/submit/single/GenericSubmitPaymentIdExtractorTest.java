package com.yolt.providers.openbanking.ais.generic2.pec.submit.single;

import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticResponse5;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticResponse5Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GenericSubmitPaymentIdExtractorTest {

    @InjectMocks
    private GenericSubmitPaymentIdExtractor subject;

    @Test
    void shouldReturnPaymentIdWhenCorrectDataAreProvided() {
        // given
        OBWriteDomesticResponse5 obWriteDomesticResponse5 = new OBWriteDomesticResponse5()
                .data(new OBWriteDomesticResponse5Data()
                        .domesticPaymentId("paymentId"));

        // when
        String result = subject.extractPaymentId(obWriteDomesticResponse5, null);

        // then
        assertThat(result).isEqualTo("paymentId");
    }
}