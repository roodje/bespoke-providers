package com.yolt.providers.stet.generic.service.payment;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.sepa.*;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;

/**
 * @deprecated It should be removed after migration of all STET generic providers to PEC (Payment Execution Context).
 * TODO: Required tickets to be done before deleting this class: C4PO-8635, C4PO-8457, C4PO-8451
 */
@Deprecated
public interface PaymentService {

    LoginUrlAndStateDTO initiatePayment(HttpClient httpClient,
                                        InitiatePaymentRequest request,
                                        DefaultAuthenticationMeans authMeans);

    SepaPaymentStatusResponseDTO confirmPayment(HttpClient httpClient,
                                                SubmitPaymentRequest request,
                                                DefaultAuthenticationMeans authMeans);

    SepaPaymentStatusResponseDTO getPaymentStatus(HttpClient httpClient,
                                                  GetStatusRequest request,
                                                  DefaultAuthenticationMeans authMeans);

    TokenResponseDTO getClientCredentialsToken(HttpClient httpClient,
                                               DefaultAuthenticationMeans authMeans,
                                               Signer signer);
}
