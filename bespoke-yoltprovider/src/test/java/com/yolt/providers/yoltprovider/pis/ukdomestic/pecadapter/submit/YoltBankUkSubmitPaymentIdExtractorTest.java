package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit;

import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit.model.PaymentSubmitResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class YoltBankUkSubmitPaymentIdExtractorTest {

    private YoltBankUkSubmitPaymentIdExtractor subject;

    @BeforeEach
    public void setup() {
        subject = new YoltBankUkSubmitPaymentIdExtractor();
    }

    @Test
    void shouldReturnPaymentIdForExtractPaymentIdWhenCorrectData() throws Throwable {
        // given
        PaymentSubmitResponse response = new PaymentSubmitResponse();
        response.setData(new PaymentSubmitResponse.Data("fakePaymentId", null, null, null));

        // when
        String result = subject.extractPaymentId(response, null);

        // then
        assertThat(result).isEqualTo("fakePaymentId");
    }
}