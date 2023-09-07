package com.yolt.providers.ing.common.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.ing.common.dto.InitiatePaymentResponse;
import com.yolt.providers.ing.common.pec.DefaultCommonPaymentStatusMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultInitiatePaymentStatusesExtractor implements PaymentStatusesExtractor<InitiatePaymentResponse, DefaultInitiatePaymentPreExecutionResult> {

    private final DefaultCommonPaymentStatusMapper commonPaymentStatusMapper;

    @Override
    public PaymentStatuses extractPaymentStatuses(final InitiatePaymentResponse initiatePaymentResponse, final DefaultInitiatePaymentPreExecutionResult ingInitiatePaymentPreExecutionResult) {
        return commonPaymentStatusMapper.mapTransactionStatus(initiatePaymentResponse.getTransactionStatus());
    }
}
