package com.yolt.providers.ing.common.pec.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.ing.common.dto.PaymentStatusResponse;
import com.yolt.providers.ing.common.pec.DefaultCommonPaymentStatusMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultSubmitPaymentStatusesExtractor implements PaymentStatusesExtractor<PaymentStatusResponse, DefaultSubmitPaymentPreExecutionResult> {

    private final DefaultCommonPaymentStatusMapper commonPaymentStatusMapper;

    @Override
    public PaymentStatuses extractPaymentStatuses(final PaymentStatusResponse paymentStatusResponse, final DefaultSubmitPaymentPreExecutionResult defaultSubmitPaymentPreExecutionResult) {
        return commonPaymentStatusMapper.mapTransactionStatus(paymentStatusResponse.getTransactionStatus());
    }
}
