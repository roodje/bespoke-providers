package com.yolt.providers.openbanking.ais.generic2.pec.mapper.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledResponse5Data;

/**
 * See https://standards.openbanking.org.uk/customer-experience-guidelines/appendices/payment-status/latest/ as a mapping referral
 */
public class GenericScheduledResponseStatusMapper implements ResponseStatusMapper<OBWriteDomesticScheduledResponse5Data.StatusEnum> {

    @Override
    public EnhancedPaymentStatus mapToEnhancedPaymentStatus(OBWriteDomesticScheduledResponse5Data.StatusEnum status) {
        return switch (status) {
            case INITIATIONPENDING -> EnhancedPaymentStatus.ACCEPTED;
            case INITIATIONCOMPLETED -> EnhancedPaymentStatus.COMPLETED;
            case INITIATIONFAILED -> EnhancedPaymentStatus.EXECUTION_FAILED;
            case CANCELLED -> EnhancedPaymentStatus.NO_CONSENT_FROM_USER;
        };
    }
}
