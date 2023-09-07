package com.yolt.providers.volksbank.common.pis.pec.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.volksbank.dto.v1_1.TransactionStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VolksbankSubmitPaymentStatusesExtractorV2 implements PaymentStatusesExtractor<com.yolt.providers.volksbank.dto.v1_1.PaymentStatus, VolksbankSepaSubmitPreExecutionResult> {

    @Override
    public PaymentStatuses extractPaymentStatuses(com.yolt.providers.volksbank.dto.v1_1.PaymentStatus paymentStatusResponse, VolksbankSepaSubmitPreExecutionResult preExecutionResult) {
        var transactionStatus = paymentStatusResponse.getTransactionStatus();
        return new PaymentStatuses(
                RawBankPaymentStatus.forStatus(transactionStatus.toString(), ""),
                mapToPaymentStatus(transactionStatus)
        );
    }

    private EnhancedPaymentStatus mapToPaymentStatus(TransactionStatus transactionStatus) {
        return switch (transactionStatus) {
            case ACTC, PATC, RCVD -> EnhancedPaymentStatus.INITIATION_SUCCESS;
            case ACCP, ACSP, ACWC, PART, PDNG -> EnhancedPaymentStatus.ACCEPTED;
            case ACSC, ACCC -> EnhancedPaymentStatus.COMPLETED;
            case CANC -> EnhancedPaymentStatus.NO_CONSENT_FROM_USER;
            case RJCT -> EnhancedPaymentStatus.REJECTED;
            case UNKNOWN -> EnhancedPaymentStatus.UNKNOWN;
        };
    }
}
