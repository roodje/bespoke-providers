package com.yolt.providers.starlingbank.common.paymentexecutioncontext.extractor;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class StarlingBankInitiatePaymentStatusesExtractorTest {

    @InjectMocks
    private StarlingBankInitiatePaymentStatusesExtractor initiatePaymentStatusesExtractor;

    @Test
    void shouldExtractPaymentStatuses() {
        //given
        String responseBody = "response";

        //when
        PaymentStatuses paymentStatusesResponse = initiatePaymentStatusesExtractor.extractPaymentStatuses(responseBody, null);

        //then
        assertThat(paymentStatusesResponse).extracting(
                        PaymentStatuses::getPaymentStatus,
                        (paymentStatuses) -> paymentStatuses.getRawBankPaymentStatus().getStatus(),
                        (paymentStatuses) -> paymentStatuses.getRawBankPaymentStatus().getReason())
                .contains(EnhancedPaymentStatus.INITIATION_SUCCESS, "UNKNOWN", "On this step there is no call to the bank, so by default INITIATION_SUCCESS is returned");
    }
}
