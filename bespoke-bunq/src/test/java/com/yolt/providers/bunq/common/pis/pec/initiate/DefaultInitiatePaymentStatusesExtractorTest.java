package com.yolt.providers.bunq.common.pis.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultInitiatePaymentStatusesExtractorTest {

    private DefaultInitiatePaymentStatusesExtractor statusesExtractor = new DefaultInitiatePaymentStatusesExtractor();

    @Test
    void shouldReturnPaymentStatuses() {
        //when
        var result = statusesExtractor.extractPaymentStatuses(null, null);

        //then
        assertThat(result).satisfies(statuses -> {
                    assertThat(statuses.getRawBankPaymentStatus().getStatus()).isEqualTo("INITIATED");
                    assertThat(statuses.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
                }
        );
    }
}