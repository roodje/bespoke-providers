package com.yolt.providers.knabgroup.common.payment;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;

public class DefaultPaymentStatusMapper {

    public PaymentStatuses mapTransactionStatus(final String rawStatus) {
        var status = SepaTransactionStatus.fromValue(rawStatus);
        return new PaymentStatuses(
                RawBankPaymentStatus.forStatus(rawStatus, ""),
                mapToEnhancedPaymentStatus(status)
        );
    }

    private EnhancedPaymentStatus mapToEnhancedPaymentStatus(final SepaTransactionStatus status) {
        return switch (status) {
            case RCVD -> EnhancedPaymentStatus.INITIATION_SUCCESS;
            case ACTC -> EnhancedPaymentStatus.ACCEPTED;
            case ACCP -> EnhancedPaymentStatus.COMPLETED;
            case RJCT -> EnhancedPaymentStatus.REJECTED;
            case CANC -> EnhancedPaymentStatus.NO_CONSENT_FROM_USER;
        };
    }
}
