package com.yolt.providers.openbanking.ais.generic2.pec.submit.scheduled;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.ResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.submit.single.GenericSubmitPaymentPreExecutionResult;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledResponse5;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledResponse5Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GenericSubmitScheduledPaymentStatusesExtractorTest {

    @InjectMocks
    private GenericSubmitScheduledPaymentStatusesExtractor subject;

    @Mock
    private ResponseStatusMapper<OBWriteDomesticScheduledResponse5Data.StatusEnum> responseStatusMapper;

    @Mock
    private GenericSubmitPaymentPreExecutionResult preExecutionResult;

    @Test
    void shouldReturnPaymentStatusesWhenCorrectDataAreProvided() {
        // given
        OBWriteDomesticScheduledResponse5 obWriteDomesticResponse5 = new OBWriteDomesticScheduledResponse5()
                .data(new OBWriteDomesticScheduledResponse5Data()
                        .status(OBWriteDomesticScheduledResponse5Data.StatusEnum.INITIATIONCOMPLETED));

        given(responseStatusMapper.mapToEnhancedPaymentStatus(OBWriteDomesticScheduledResponse5Data.StatusEnum.INITIATIONCOMPLETED))
                .willReturn(EnhancedPaymentStatus.COMPLETED);

        PaymentStatuses expectedResult = new PaymentStatuses(RawBankPaymentStatus.forStatus("InitiationCompleted", ""),
                EnhancedPaymentStatus.COMPLETED);

        // when
        PaymentStatuses result = subject.extractPaymentStatuses(obWriteDomesticResponse5, preExecutionResult);

        // then
        then(responseStatusMapper);

        assertThat(result).usingRecursiveComparison().isEqualTo(expectedResult);
    }

}