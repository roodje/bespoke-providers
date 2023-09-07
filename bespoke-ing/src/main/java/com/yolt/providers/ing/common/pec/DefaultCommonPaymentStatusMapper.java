package com.yolt.providers.ing.common.pec;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.ing.common.dto.SepaTransactionStatus;

public class DefaultCommonPaymentStatusMapper {

    public PaymentStatuses mapTransactionStatus(final String rawStatus) {
        var status = SepaTransactionStatus.fromValue(rawStatus);
        return new PaymentStatuses(
                RawBankPaymentStatus.forStatus(rawStatus, ""),
                mapToEnhancedPaymentStatus(status)
        );
    }

    private EnhancedPaymentStatus mapToEnhancedPaymentStatus(final SepaTransactionStatus status) {
        return switch (status) {
            case PDNG, RCVD, ACTC, PATC -> EnhancedPaymentStatus.INITIATION_SUCCESS;
            case ACCC, ACCP, ACFC, ACSP, ACWC, ACWP, PART, ACTV -> EnhancedPaymentStatus.ACCEPTED;
            case ACSC, EXPI -> EnhancedPaymentStatus.COMPLETED;
            case RJCT -> EnhancedPaymentStatus.REJECTED;
            case CANC -> EnhancedPaymentStatus.NO_CONSENT_FROM_USER;
        };
    }
}
