package com.yolt.providers.openbanking.ais.generic2.pec.status.scheduled;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericDelegatingScheduledPaymentStatusResponseMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.ScheduledPaymentStatusResponse;
import com.yolt.providers.openbanking.ais.generic2.pec.status.single.GenericPaymentStatusPreExecutionResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GenericScheduledPaymentStatusStatusesExtractor implements PaymentStatusesExtractor<ScheduledPaymentStatusResponse, GenericPaymentStatusPreExecutionResult> {

    private final GenericDelegatingScheduledPaymentStatusResponseMapper paymentStatusResponseMapper;

    @Override
    public PaymentStatuses extractPaymentStatuses(ScheduledPaymentStatusResponse paymentStatusResponse, GenericPaymentStatusPreExecutionResult preExecutionResult) {
        ScheduledPaymentStatusResponse.Data.Status status = paymentStatusResponse.getData().getStatus();
        return new PaymentStatuses(RawBankPaymentStatus.forStatus(status.toString(), ""),
                paymentStatusResponseMapper.mapToEnhancedPaymentStatus(status));
    }
}
