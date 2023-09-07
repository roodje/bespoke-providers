package com.yolt.providers.openbanking.ais.generic2.pec.common;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Collections;

@RequiredArgsConstructor
public class GenericPaymentHttpHeadersFactory implements PaymentHttpHeadersFactory {

    private final PaymentRequestSigner paymentRequestSigner;
    private final PaymentRequestIdempotentKeyProvider requestIdempotentKeyProvider;

    @Override
    public HttpHeaders createPaymentHttpHeaders(String accessToken,
                                                DefaultAuthMeans authMeans,
                                                Signer signer,
                                                Object body) {
        var headers = createCommonPaymentHttpHeaders(accessToken, authMeans);
        headers.add(HttpExtraHeaders.SIGNATURE_HEADER_NAME, paymentRequestSigner.createRequestSignature(body, authMeans, signer));
        return headers;
    }

    @Override
    public HttpHeaders createCommonPaymentHttpHeaders(String accessToken,
                                                      DefaultAuthMeans authMeans) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add(HttpExtraHeaders.FINANCIAL_ID_HEADER_NAME, authMeans.getInstitutionId());
        headers.add(HttpExtraHeaders.IDEMPOTENT_KEY, requestIdempotentKeyProvider.provideIdempotentKey());
        return headers;
    }
}
