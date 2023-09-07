package com.yolt.providers.cbiglobe.common.pis.pec;

import com.yolt.providers.cbiglobe.common.model.TransactionStatus;
import com.yolt.providers.cbiglobe.pis.dto.PaymentInitiationRequestResponseType;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;

public abstract class CbiGlobeInitiatePaymentCommonExtractor<T> implements PaymentStatusesExtractor<PaymentInitiationRequestResponseType, T> {

    @Override
    public PaymentStatuses extractPaymentStatuses(PaymentInitiationRequestResponseType initiatePaymentResponse, T preExecutionResult) {
        var transactionStatus = TransactionStatus.valueOf(initiatePaymentResponse.getTransactionStatus());
        return new PaymentStatuses(
                RawBankPaymentStatus.forStatus(transactionStatus.toString(), ""),
                mapToPaymentStatus(transactionStatus)
        );
    }

    private EnhancedPaymentStatus mapToPaymentStatus(TransactionStatus transactionStatus) {
        return switch (transactionStatus) {
            case RCVD, DAS_I, DAS_SR, DAS_CR -> EnhancedPaymentStatus.INITIATION_SUCCESS;
            case PDNG, ACCP, ACSP, ACTC, ACWC, ACWP -> EnhancedPaymentStatus.ACCEPTED;
            case ACSC -> EnhancedPaymentStatus.COMPLETED;
            case RJCT, DAS_FAILED -> EnhancedPaymentStatus.REJECTED;
        };
    }
}
