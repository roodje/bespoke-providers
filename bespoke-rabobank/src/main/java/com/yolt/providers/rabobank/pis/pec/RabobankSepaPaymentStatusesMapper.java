package com.yolt.providers.rabobank.pis.pec;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.rabobank.dto.external.TransactionStatus;

public class RabobankSepaPaymentStatusesMapper {

    public EnhancedPaymentStatus mapToInternalPaymentStatus(TransactionStatus transactionStatus) {
        return switch (transactionStatus) {
            case RCVD, ACTC, PDNG -> EnhancedPaymentStatus.INITIATION_SUCCESS;
            case ACSP -> EnhancedPaymentStatus.ACCEPTED;
            case ACCC, ACSC -> EnhancedPaymentStatus.COMPLETED;
            case CANC -> EnhancedPaymentStatus.NO_CONSENT_FROM_USER;
            case RJCT -> EnhancedPaymentStatus.REJECTED;
            default -> EnhancedPaymentStatus.UNKNOWN;
        };
    }
}
