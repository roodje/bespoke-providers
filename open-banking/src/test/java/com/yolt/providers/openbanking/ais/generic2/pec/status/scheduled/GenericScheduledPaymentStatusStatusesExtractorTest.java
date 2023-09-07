package com.yolt.providers.openbanking.ais.generic2.pec.status.scheduled;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericDelegatingScheduledPaymentStatusResponseMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.ScheduledPaymentStatusResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GenericScheduledPaymentStatusStatusesExtractorTest {

    @InjectMocks
    GenericScheduledPaymentStatusStatusesExtractor subject;

    @Mock
    GenericDelegatingScheduledPaymentStatusResponseMapper delegatingScheduledPaymentStatusResponseMapper;

    @Test
    void shouldReturnPaymentStatusesWhenDataAreProvided() {
        //given
        given(delegatingScheduledPaymentStatusResponseMapper.mapToEnhancedPaymentStatus(ScheduledPaymentStatusResponse.Data.Status.INITIATIONPENDING))
                .willReturn(EnhancedPaymentStatus.ACCEPTED);
        ScheduledPaymentStatusResponse scheduledPaymentStatusResponse = new ScheduledPaymentStatusResponse();
        scheduledPaymentStatusResponse.setData(
                new ScheduledPaymentStatusResponse.Data(
                        null,
                        null,
                        ScheduledPaymentStatusResponse.Data.Status.INITIATIONPENDING));
        PaymentStatuses expectedResult = new PaymentStatuses(RawBankPaymentStatus.forStatus("InitiationPending", ""),
                EnhancedPaymentStatus.ACCEPTED);

        //when
        PaymentStatuses result = subject.extractPaymentStatuses(scheduledPaymentStatusResponse, null);

        //then
        assertThat(result).usingRecursiveComparison().isEqualTo(expectedResult);
    }

}