package com.yolt.providers.cbiglobe.common.pis.pec;

import com.yolt.providers.cbiglobe.common.model.TransactionStatus;
import com.yolt.providers.cbiglobe.pis.dto.GetPaymentStatusRequestResponseType;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;

public abstract class CbiGlobeStatusPaymentCommonExtractor<T> implements PaymentStatusesExtractor<GetPaymentStatusRequestResponseType, T> {

    @Override
    public PaymentStatuses extractPaymentStatuses(GetPaymentStatusRequestResponseType initiatePaymentResponse, T preExecutionResult) {
        var transactionStatus = TransactionStatus.valueOf(initiatePaymentResponse.getTransactionStatus());
        return new PaymentStatuses(
                RawBankPaymentStatus.forStatus(transactionStatus.toString(), ""),
                mapToPaymentStatus(transactionStatus)
        );
    }

    private EnhancedPaymentStatus mapToPaymentStatus(TransactionStatus transactionStatus) {
        return switch (transactionStatus) {
            case RCVD, ACTC, PDNG, DAS_I, DAS_SR, DAS_CR -> EnhancedPaymentStatus.INITIATION_SUCCESS;
            case ACCP, ACSP, ACWC -> EnhancedPaymentStatus.ACCEPTED;
            case ACSC, ACWP -> EnhancedPaymentStatus.COMPLETED;
            case RJCT, DAS_FAILED -> EnhancedPaymentStatus.REJECTED;
        };
    }
}
