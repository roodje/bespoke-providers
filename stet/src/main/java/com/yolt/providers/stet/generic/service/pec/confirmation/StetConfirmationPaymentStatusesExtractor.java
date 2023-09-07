package com.yolt.providers.stet.generic.service.pec.confirmation;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentStatus;

public abstract class StetConfirmationPaymentStatusesExtractor {

    public PaymentStatuses mapToPaymentStatuses(StetPaymentStatus paymentStatus) {
        return new PaymentStatuses(
                RawBankPaymentStatus.forStatus(String.valueOf(paymentStatus), ""),
                mapToPaymentStatus(paymentStatus));
    }

    public EnhancedPaymentStatus mapToPaymentStatus(StetPaymentStatus paymentStatus) {
        return switch (paymentStatus) {
            case RCVD, PDNG, ACTC -> EnhancedPaymentStatus.INITIATION_SUCCESS;
            case ACCP, ACSP, ACWC, PART, PATC -> EnhancedPaymentStatus.ACCEPTED;
            case ACSC, ACWP -> EnhancedPaymentStatus.COMPLETED;
            case CANC -> EnhancedPaymentStatus.NO_CONSENT_FROM_USER;
            case RJCT -> EnhancedPaymentStatus.REJECTED;
        };
    }
}
