package com.yolt.providers.fineco.pis;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.fineco.dto.PaymentStatus;

public class FinecoPaymentStatusMapper {

    public PaymentStatuses mapPaymentStatus(String rawStatus) {
        var status = PaymentStatus.fromValue(rawStatus);
        return new PaymentStatuses(
                RawBankPaymentStatus.forStatus(rawStatus, ""),
                mapToEnhancedPaymentStatus(status)
        );
    }

    private EnhancedPaymentStatus mapToEnhancedPaymentStatus(PaymentStatus status) {
        return switch (status) {
            case PDNG, RCVD -> EnhancedPaymentStatus.INITIATION_SUCCESS;
            case ACCC, ACCP, ACFC, ACSP, ACWC, ACWP, ACTC, PATC -> EnhancedPaymentStatus.ACCEPTED;
            case ACSC -> EnhancedPaymentStatus.COMPLETED;
            case RJCT -> EnhancedPaymentStatus.REJECTED;
            case CANC -> EnhancedPaymentStatus.NO_CONSENT_FROM_USER;
        };
    }
}