package com.yolt.providers.openbanking.ais.generic2.pec.mapper.status;

import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledConsentResponse5Data;

/**
 * See https://standards.openbanking.org.uk/customer-experience-guidelines/appendices/payment-status/latest/ as a mapping referral
 */
public class GenericScheduledConsentResponseStatusMapper implements ResponseStatusMapper<OBWriteDomesticScheduledConsentResponse5Data.StatusEnum> {

    @Override
    public EnhancedPaymentStatus mapToEnhancedPaymentStatus(OBWriteDomesticScheduledConsentResponse5Data.StatusEnum status) {
        return switch (status) {
            case AWAITINGAUTHORISATION, AUTHORISED, CONSUMED -> EnhancedPaymentStatus.INITIATION_SUCCESS;
            case REJECTED -> EnhancedPaymentStatus.REJECTED;
        };
    }
}
