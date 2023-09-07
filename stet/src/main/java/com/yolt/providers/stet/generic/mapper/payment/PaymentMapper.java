package com.yolt.providers.stet.generic.mapper.payment;

import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatus;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentStatus;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentInitiationRequestDTO;

/**
 * @deprecated It should be removed after migration of all STET generic providers to PEC (Payment Execution Context).
 * TODO: Required tickets to be done before deleting this class: C4PO-8635, C4PO-8457, C4PO-8451
 */
@Deprecated
public interface PaymentMapper {

    StetPaymentInitiationRequestDTO mapToStetPaymentInitiationRequestDTO(InitiatePaymentRequest request,
                                                                         DefaultAuthenticationMeans authMeans);

    SepaPaymentStatus mapToSepaPaymentStatus(StetPaymentStatus stetPaymentStatus);
}
