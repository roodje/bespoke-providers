package com.yolt.providers.yoltprovider.pis.ukdomestic;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class UkDomesticPaymentStatusMapper {

    private static final String PENDING = "Pending";
    private static final String ACCEPTED_SETTLEMENT_IN_PROCESS = "AcceptedSettlementInProcess";
    private static final String ACCEPTED_SETTLEMENT_COMPLETED = "AcceptedSettlementCompleted";
    private static final String REJECTED = "Rejected";

    public EnhancedPaymentStatus mapToInternalPaymentStatus(String status) {
        return switch (status) {
            case PENDING -> EnhancedPaymentStatus.INITIATION_SUCCESS;
            case ACCEPTED_SETTLEMENT_IN_PROCESS -> EnhancedPaymentStatus.ACCEPTED;
            case ACCEPTED_SETTLEMENT_COMPLETED -> EnhancedPaymentStatus.COMPLETED;
            case REJECTED -> EnhancedPaymentStatus.REJECTED;
            default -> throw new IllegalStateException("Unrecognized payment status: " + status);
        };
    }
}
