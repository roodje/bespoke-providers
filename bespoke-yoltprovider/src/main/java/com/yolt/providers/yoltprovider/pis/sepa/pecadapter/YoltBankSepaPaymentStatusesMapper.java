package com.yolt.providers.yoltprovider.pis.sepa.pecadapter;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatus;

public class YoltBankSepaPaymentStatusesMapper {

    public EnhancedPaymentStatus mapToInternalPaymentStatus(SepaPaymentStatus sepaPaymentStatus) {
        return switch (sepaPaymentStatus) {
            case INITIATED -> EnhancedPaymentStatus.INITIATION_SUCCESS;
            case ACCEPTED -> EnhancedPaymentStatus.ACCEPTED;
            case COMPLETED -> EnhancedPaymentStatus.COMPLETED;
            case REJECTED -> EnhancedPaymentStatus.REJECTED;
        };
    }
}
