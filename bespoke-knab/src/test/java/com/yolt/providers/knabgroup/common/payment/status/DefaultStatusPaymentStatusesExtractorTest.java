package com.yolt.providers.knabgroup.common.payment.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.knabgroup.common.payment.DefaultPaymentStatusMapper;
import com.yolt.providers.knabgroup.common.payment.dto.external.StatusPaymentResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultStatusPaymentStatusesExtractorTest {

    private DefaultStatusPaymentStatusesExtractor subject;

    @Mock
    private DefaultPaymentStatusMapper statusMapper;

    @Test
    void shouldReturnCorrectlyMappedPaymentStatusesForKnownStatusValue() {
        // given
        PaymentStatuses mappedStatus = new PaymentStatuses(RawBankPaymentStatus.forStatus("status", "reason"), EnhancedPaymentStatus.COMPLETED);
        when(statusMapper.mapTransactionStatus(anyString())).thenReturn(mappedStatus);
        subject = new DefaultStatusPaymentStatusesExtractor(statusMapper);

        // when
        PaymentStatuses result = subject.extractPaymentStatuses(new StatusPaymentResponse("transactionStatus"), null);

        // then
        assertThat(result).isEqualTo(mappedStatus);
    }
}