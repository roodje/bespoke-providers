package com.yolt.providers.openbanking.ais.generic2.pec.submit.single;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.ResponseStatusMapper;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticResponse5;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticResponse5Data;
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
class GenericSubmitPaymentStatusesExtractorTest {

    @InjectMocks
    private GenericSubmitPaymentStatusesExtractor subject;

    @Mock
    private ResponseStatusMapper<OBWriteDomesticResponse5Data.StatusEnum> responseStatusMapper;

    @Mock
    private GenericSubmitPaymentPreExecutionResult preExecutionResult;

    @Test
    void shouldReturnPaymentStatusesWhenCorrectDataAreProvided() {
        // given
        OBWriteDomesticResponse5 obWriteDomesticResponse5 = new OBWriteDomesticResponse5()
                .data(new OBWriteDomesticResponse5Data()
                        .status(OBWriteDomesticResponse5Data.StatusEnum.PENDING));

        given(responseStatusMapper.mapToEnhancedPaymentStatus(any(OBWriteDomesticResponse5Data.StatusEnum.class)))
                .willReturn(EnhancedPaymentStatus.INITIATION_SUCCESS);

        // when
        PaymentStatuses result = subject.extractPaymentStatuses(obWriteDomesticResponse5, preExecutionResult);

        // then
        then(responseStatusMapper)
                .should()
                .mapToEnhancedPaymentStatus(OBWriteDomesticResponse5Data.StatusEnum.PENDING);

        assertThat(result.getRawBankPaymentStatus()).satisfies(rawBankPaymentStatus -> {
            assertThat(rawBankPaymentStatus.getStatus()).isEqualTo(OBWriteDomesticResponse5Data.StatusEnum.PENDING.toString());
            assertThat(rawBankPaymentStatus.getReason()).isEmpty();
        });
        assertThat(result.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.INITIATION_SUCCESS);
    }

    @Test
    void shouldReturnPaymentStatusesWhenStatusIsRejected() {
        // given
        OBWriteDomesticResponse5 obWriteDomesticResponse5 = new OBWriteDomesticResponse5()
                .data(new OBWriteDomesticResponse5Data()
                        .status(OBWriteDomesticResponse5Data.StatusEnum.REJECTED));
        given(responseStatusMapper.mapToEnhancedPaymentStatus(any(OBWriteDomesticResponse5Data.StatusEnum.class)))
                .willReturn(EnhancedPaymentStatus.REJECTED);

        // when
        PaymentStatuses result = subject.extractPaymentStatuses(obWriteDomesticResponse5, preExecutionResult);

        // then
        then(responseStatusMapper)
                .should()
                .mapToEnhancedPaymentStatus(OBWriteDomesticResponse5Data.StatusEnum.REJECTED);

        assertThat(result.getRawBankPaymentStatus()).satisfies(rawBankPaymentStatus -> {
            assertThat(rawBankPaymentStatus.getStatus()).isEqualTo(OBWriteDomesticResponse5Data.StatusEnum.REJECTED.toString());
            assertThat(rawBankPaymentStatus.getReason()).isEmpty();
        });
        assertThat(result.getPaymentStatus()).isEqualTo(EnhancedPaymentStatus.REJECTED);
    }
}