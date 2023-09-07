package com.yolt.providers.starlingbank.common.paymentexecutioncontext.extractor;

import com.yolt.providers.starlingbank.common.model.ConsentInformation;
import com.yolt.providers.starlingbank.common.model.PaymentSubmissionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class StarlingBankPaymentIdExtractorTest {
    private static final UUID PAYMENT_ORDER_UUID = UUID.randomUUID();

    @InjectMocks
    private StarlingBankPaymentIdExtractor paymentIdExtractor;

    @Test
    void shouldExtractPaymentId() {
        //given
        PaymentSubmissionResponse paymentSubmissionResponse = new PaymentSubmissionResponse(
                PAYMENT_ORDER_UUID,
                new ConsentInformation()
        );

        //when
        String result = paymentIdExtractor.extractPaymentId(paymentSubmissionResponse, null);

        //then
        assertThat(result).isEqualTo(PAYMENT_ORDER_UUID.toString());
    }
}
