package com.yolt.providers.stet.generic.dto.payment;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

/**
 * @deprecated It should be removed after migration of all STET generic providers to PEC (Payment Execution Context).
 * TODO: Required tickets to be done before deleting this class: C4PO-8635, C4PO-8457, C4PO-8451
 */
@Deprecated
@ProjectedPayload
public interface StetPaymentStatusResponseDTO {

    @JsonPath("$.paymentRequest.paymentInformationStatus")
    StetPaymentStatus getPaymentStatus();
}
