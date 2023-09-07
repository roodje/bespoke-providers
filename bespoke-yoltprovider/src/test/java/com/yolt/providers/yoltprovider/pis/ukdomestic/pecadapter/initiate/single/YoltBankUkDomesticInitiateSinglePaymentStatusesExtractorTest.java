package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.yoltprovider.pis.ukdomestic.InitiatePaymentConsentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class YoltBankUkDomesticInitiateSinglePaymentStatusesExtractorTest {

    private YoltBankUkDomesticInitiateSinglePaymentStatusesExtractor paymentStatusesExtractor;

    @BeforeEach
    public void setup() {
        paymentStatusesExtractor = new YoltBankUkDomesticInitiateSinglePaymentStatusesExtractor();
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
