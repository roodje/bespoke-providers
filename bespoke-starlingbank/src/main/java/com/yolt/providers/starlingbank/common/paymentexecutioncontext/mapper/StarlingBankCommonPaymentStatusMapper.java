package com.yolt.providers.starlingbank.common.paymentexecutioncontext.mapper;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.starlingbank.common.model.PaymentStatusDetails;

public class StarlingBankCommonPaymentStatusMapper {

    public PaymentStatuses mapTransactionStatus(final PaymentStatusDetails paymentStatusDetails) {
        var status = paymentStatusDetails.getPaymentStatus();
        var reason = paymentStatusDetails.getDescription();
        return new PaymentStatuses(
                RawBankPaymentStatus.forStatus(status, reason),
                mapToEnhancedPaymentStatus(status)
        );
    }

    private EnhancedPaymentStatus mapToEnhancedPaymentStatus(final String status) {
        return switch (status.toUpperCase()) {
            case "PENDING" -> EnhancedPaymentStatus.ACCEPTED;
            case "ACCEPTED" -> EnhancedPaymentStatus.COMPLETED;
            case "REJECTED" -> EnhancedPaymentStatus.REJECTED;
            default -> throw new IllegalStateException("Unexpected value: " + status.toLowerCase());
        };
    }
}
