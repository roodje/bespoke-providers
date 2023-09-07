package com.yolt.providers.stet.generic.service.payment.request;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @deprecated It should be removed after migration of all STET generic providers to PEC (Payment Execution Context).
 * TODO: Required tickets to be done before deleting this class: C4PO-8635, C4PO-8457, C4PO-8451
 */
@Deprecated
@Getter
@AllArgsConstructor
public class PaymentRequest {

    private String url;
    private String accessToken;
    private Signer signer;
    private String psuIpAddress;
    private DefaultAuthenticationMeans authMeans;
}
