package com.yolt.providers.volksbank.common.pis.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.volksbank.dto.v1_1.InitiatePaymentResponse;

public class VolksbankInitiatePaymentStatusesExtractorV2 implements PaymentStatusesExtractor<InitiatePaymentResponse, VolksbankSepaInitiatePreExecutionResult> {

    @Override
    public PaymentStatuses extractPaymentStatuses(InitiatePaymentResponse initiatePaymentResponse, VolksbankSepaInitiatePreExecutionResult preExecutionResult) {
        var transactionStatus = initiatePaymentResponse.getTransactionStatus();
        return new PaymentStatuses(
                RawBankPaymentStatus.forStatus(transactionStatus.toString(), ""),
                mapToPaymentStatus(transactionStatus)
        );
    }

    private EnhancedPaymentStatus mapToPaymentStatus(InitiatePaymentResponse.TransactionStatusEnum transactionStatus) {
        return switch (transactionStatus) {
            case RCVD -> EnhancedPaymentStatus.INITIATION_SUCCESS;
            case PDNG -> EnhancedPaymentStatus.ACCEPTED;
            case ACSC, ACCC -> EnhancedPaymentStatus.COMPLETED;
            case CANC -> EnhancedPaymentStatus.NO_CONSENT_FROM_USER;
            case REJECTED -> EnhancedPaymentStatus.REJECTED;
            case UNKNOWN -> EnhancedPaymentStatus.UNKNOWN;
        };
    }
}
