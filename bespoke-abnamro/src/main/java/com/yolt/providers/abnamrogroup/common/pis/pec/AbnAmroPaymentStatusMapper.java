package com.yolt.providers.abnamrogroup.common.pis.pec;

import com.yolt.providers.abnamro.pis.TransactionStatusResponse;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;

public class AbnAmroPaymentStatusMapper {

    public PaymentStatuses mapBankPaymentStatus(TransactionStatusResponse.StatusEnum bankStatus) {
        return new PaymentStatuses(RawBankPaymentStatus.forStatus(bankStatus.toString(), ""),
                mapToEnhancedPaymentStatus(bankStatus));
    }

    private EnhancedPaymentStatus mapToEnhancedPaymentStatus(TransactionStatusResponse.StatusEnum bankStatus) {
        return switch (bankStatus) {
            case AUTHORIZED, STORED -> EnhancedPaymentStatus.INITIATION_SUCCESS;
            case INPROGRESS, FUTURE, SCHEDULED -> EnhancedPaymentStatus.ACCEPTED;
            case EXECUTED -> EnhancedPaymentStatus.COMPLETED;
            case REJECTED -> EnhancedPaymentStatus.REJECTED;
            case UNKNOWN -> EnhancedPaymentStatus.UNKNOWN;
        };
    }
}
