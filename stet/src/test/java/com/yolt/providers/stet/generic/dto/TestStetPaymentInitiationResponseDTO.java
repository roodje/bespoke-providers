package com.yolt.providers.stet.generic.dto;

import com.yolt.providers.stet.generic.dto.payment.StetPaymentInitiationResponseDTO;
import lombok.Builder;
import lombok.Getter;

/**
 * @deprecated It should be removed after migration of all STET generic providers to PEC (Payment Execution Context).
 * TODO: Required tickets to be done before deleting this class: C4PO-8635, C4PO-8457, C4PO-8451
 */
@Deprecated
@Getter
@Builder
public class TestStetPaymentInitiationResponseDTO implements StetPaymentInitiationResponseDTO {

    private String consentApprovalHref;
}
