package com.yolt.providers.knabgroup.common.payment.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.knabgroup.common.payment.DefaultPaymentStatusMapper;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.StatusPaymentPreExecutionResult;
import com.yolt.providers.knabgroup.common.payment.dto.external.StatusPaymentResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultStatusPaymentStatusesExtractor implements PaymentStatusesExtractor<StatusPaymentResponse, StatusPaymentPreExecutionResult> {

    private final DefaultPaymentStatusMapper paymentStatusMapper;

    @Override
    public PaymentStatuses extractPaymentStatuses(final StatusPaymentResponse response, final StatusPaymentPreExecutionResult preExecutionResult) {
        return paymentStatusMapper.mapTransactionStatus(response.getTransactionStatus());
    }
}
