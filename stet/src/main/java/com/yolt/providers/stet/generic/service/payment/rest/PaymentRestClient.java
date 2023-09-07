package com.yolt.providers.stet.generic.service.payment.rest;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.dto.payment.*;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.service.payment.request.PaymentRequest;
import org.springframework.util.MultiValueMap;

/**
 * @deprecated It should be removed after migration of all STET generic providers to PEC (Payment Execution Context).
 * TODO: Required tickets to be done before deleting this class: C4PO-8635, C4PO-8457, C4PO-8451
 */
@Deprecated
public interface PaymentRestClient {

    TokenResponseDTO getClientToken(HttpClient httpClient,
                                    String url,
                                    DefaultAuthenticationMeans authMeans,
                                    MultiValueMap<String, String> body,
                                    Signer signer);

    StetPaymentInitiationResponseDTO initiatePayment(HttpClient httpClient,
                                                     PaymentRequest paymentRequest,
                                                     StetPaymentInitiationRequestDTO body);

    StetPaymentConfirmationResponseDTO confirmPayment(HttpClient httpClient,
                                                      PaymentRequest paymentRequest,
                                                      StetPaymentConfirmationRequestDTO body);

    StetPaymentStatusResponseDTO getPaymentStatus(HttpClient httpClient, PaymentRequest paymentRequest);
}