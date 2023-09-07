package com.yolt.providers.openbanking.ais.generic2.pec.status.single;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.GenericDelegatingPaymentStatusResponseMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.status.model.PaymentStatusResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GenericPaymentStatusStatusesExtractor implements PaymentStatusesExtractor<PaymentStatusResponse, GenericPaymentStatusPreExecutionResult> {

    private final GenericDelegatingPaymentStatusResponseMapper paymentStatusResponseMapper;

    @Override
    public PaymentStatuses extractPaymentStatuses(PaymentStatusResponse paymentStatusResponse, GenericPaymentStatusPreExecutionResult preExecutionResult) {
        PaymentStatusResponse.Data.Status status = paymentStatusResponse.getData().getStatus();
        return new PaymentStatuses(RawBankPaymentStatus.forStatus(status.toString(), ""),
                paymentStatusResponseMapper.mapToEnhancedPaymentStatus(status));
    }
}
