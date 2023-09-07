package com.yolt.providers.abnamrogroup.common.pis.pec;

import com.yolt.providers.abnamro.pis.TransactionStatusResponse;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AbnAmroStatusesExtractorTest {

    @InjectMocks
    private AbnAmroStatusesExtractor<String> subject;

    @Mock
    private AbnAmroPaymentStatusMapper paymentStatusMapper;

    @Test
    void shouldReturnProperPaymentStatusesWhenCorrectDataAreProvided() {
        // given
        TransactionStatusResponse transactionStatusResponse = new TransactionStatusResponse();
        TransactionStatusResponse.StatusEnum status = TransactionStatusResponse.StatusEnum.STORED;
        transactionStatusResponse.setStatus(status);
        PaymentStatuses paymentStatuses = new PaymentStatuses(RawBankPaymentStatus.forStatus("STORED", ""),
                EnhancedPaymentStatus.INITIATION_SUCCESS);

        given(paymentStatusMapper.mapBankPaymentStatus(any(TransactionStatusResponse.StatusEnum.class)))
                .willReturn(paymentStatuses);

        // when
        PaymentStatuses result = subject.extractPaymentStatuses(transactionStatusResponse, null);

        // then
        then(paymentStatusMapper)
                .should()
                .mapBankPaymentStatus(status);
        assertThat(result).isEqualTo(paymentStatuses);
    }
}