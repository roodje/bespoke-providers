package com.yolt.providers.stet.generic.service.payment.rest.header;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.service.payment.request.PaymentRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

/**
 * @deprecated It should be removed after migration of all STET generic providers to PEC (Payment Execution Context).
 * TODO: Required tickets to be done before deleting this class: C4PO-8635, C4PO-8457, C4PO-8451
 */
@Deprecated
public interface PaymentHttpHeadersFactory {

    HttpHeaders createClientTokenHeaders(DefaultAuthenticationMeans authMeans, MultiValueMap<String, String> body, Signer signer, String url);

    HttpHeaders createPaymentInitiationHeaders(HttpMethod method, PaymentRequest paymentRequest, Object requestBody);

    HttpHeaders createPaymentConfirmationHeaders(HttpMethod method, PaymentRequest paymentRequest, Object requestBody);

    HttpHeaders createPaymentStatusHeaders(HttpMethod method, PaymentRequest paymentRequest);
}
