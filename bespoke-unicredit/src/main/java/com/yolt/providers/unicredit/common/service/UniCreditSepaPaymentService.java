package com.yolt.providers.unicredit.common.service;

import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.unicredit.common.util.ProviderInfo;

public interface UniCreditSepaPaymentService {
    LoginUrlAndStateDTO initiatePayment(InitiatePaymentRequest initiatePaymentRequest,
                                        ProviderInfo providerInfo) throws CreationFailedException;
    SepaPaymentStatusResponseDTO submitPayment(SubmitPaymentRequest submitPaymentRequest,
                                               ProviderInfo providerInfo) throws ConfirmationFailedException;
    SepaPaymentStatusResponseDTO getStatus(GetStatusRequest getStatusRequest,
                                           ProviderInfo providerInfo) throws ConfirmationFailedException;
}
