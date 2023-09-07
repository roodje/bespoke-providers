package com.yolt.providers.openbanking.ais.generic2.pec.common;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import org.springframework.http.HttpHeaders;

public interface PaymentHttpHeadersFactory {

    HttpHeaders createPaymentHttpHeaders(String accessToken,
                                         DefaultAuthMeans authMeans,
                                         Signer signer,
                                         Object body);

    HttpHeaders createCommonPaymentHttpHeaders(String accessToken,
                                               DefaultAuthMeans authMeans);
}
