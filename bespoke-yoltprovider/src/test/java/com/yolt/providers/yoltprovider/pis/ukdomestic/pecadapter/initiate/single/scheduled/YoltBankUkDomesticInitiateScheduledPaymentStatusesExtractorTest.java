package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single.scheduled;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.yoltprovider.pis.ukdomestic.InitiatePaymentConsentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class YoltBankUkDomesticInitiateScheduledPaymentStatusesExtractorTest {

    private YoltBankUkDomesticInitiateScheduledPaymentStatusesExtractor paymentStatusesExtractor;

    @BeforeEach
    public void setup() {
        paymentStatusesExtractor = new YoltBankUkDomesticInitiateScheduledPaymentStatusesExtractor();
    }

    @Test
    void shouldReturnProperPaymentStatuses() {
        // given
        InitiatePaymentConsentResponse responseBody = new InitiatePaymentConsentResponse(
                "",
                ""
        );

        // when
        PaymentStatuses result = paymentStatusesExtractor.extractPaymentStatuses(responseBody, null);

        // then
        assertThat(result.getRawBankPaymentStatus()).extracting(RawBankPaymentStatus::getStatus, RawBankPaymentStatus::getReason)
                .contains("UNKNOWN", "");
        assertThat(result.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
    }
}
