package com.yolt.providers.abnamrogroup.common.pis.pec.initiate;

import com.yolt.providers.abnamrogroup.common.pis.InitiatePaymentResponseDTO;
import com.yolt.providers.abnamro.pis.TransactionStatusResponse;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPaymentStatusMapper;
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
class AbnAmroInitiatePaymentStatusesExtractorTest {

    @InjectMocks
    private AbnAmroInitiatePaymentStatusesExtractor subject;

    @Mock
    private AbnAmroPaymentStatusMapper paymentStatusMapper;

    @Test
    void shouldReturnProperPaymentStatusesWhenCorrectDataAreProvided() {
        // given
        InitiatePaymentResponseDTO initiatePaymentResponseDTO = new InitiatePaymentResponseDTO("", "", "STORED", "", "");
        PaymentStatuses paymentStatuses = new PaymentStatuses(RawBankPaymentStatus.forStatus("STORED", ""),
                EnhancedPaymentStatus.INITIATION_SUCCESS);

        given(paymentStatusMapper.mapBankPaymentStatus(any(TransactionStatusResponse.StatusEnum.class)))
                .willReturn(paymentStatuses);

        // when
        PaymentStatuses result = subject.extractPaymentStatuses(initiatePaymentResponseDTO, null);

        // then
        then(paymentStatusMapper)
                .should()
                .mapBankPaymentStatus(TransactionStatusResponse.StatusEnum.STORED);
        assertThat(result).isEqualTo(paymentStatuses);
    }
}