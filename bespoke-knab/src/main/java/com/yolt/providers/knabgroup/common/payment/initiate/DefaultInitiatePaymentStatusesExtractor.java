package com.yolt.providers.knabgroup.common.payment.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.knabgroup.common.payment.DefaultPaymentStatusMapper;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.InitiatePaymentPreExecutionResult;
import com.yolt.providers.knabgroup.common.payment.dto.external.InitiatePaymentResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultInitiatePaymentStatusesExtractor implements PaymentStatusesExtractor<InitiatePaymentResponse, InitiatePaymentPreExecutionResult> {

    private final DefaultPaymentStatusMapper paymentStatusMapper;

    @Override
    public PaymentStatuses extractPaymentStatuses(final InitiatePaymentResponse response, final InitiatePaymentPreExecutionResult preExecutionResult) {
        return paymentStatusMapper.mapTransactionStatus(response.getTransactionStatus());
    }
}
