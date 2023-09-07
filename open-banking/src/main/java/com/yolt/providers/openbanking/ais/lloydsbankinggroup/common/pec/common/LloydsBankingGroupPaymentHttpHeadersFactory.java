package com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.pec.common;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpExtraHeaders;
import com.yolt.providers.openbanking.ais.generic2.pec.common.PaymentHttpHeadersFactory;
import com.yolt.providers.openbanking.ais.generic2.pec.common.PaymentRequestIdempotentKeyProvider;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RequiredArgsConstructor
public class LloydsBankingGroupPaymentHttpHeadersFactory implements PaymentHttpHeadersFactory {

    private static final String X_LBG_CHANNEL_HEADER_NAME = "x-lbg-channel";
    private static final String RC_LBG_CHANNEL = "RC";

    private final PaymentRequestSigner paymentRequestSigner;
    private final PaymentRequestIdempotentKeyProvider requestIdempotentKeyProvider;

    @Override
    public HttpHeaders createPaymentHttpHeaders(String accessToken,
                                                DefaultAuthMeans authMeans,
                                                Signer signer,
                                                Object body) {
        var headers = createCommonPaymentHttpHeaders(accessToken, authMeans);
        headers.add(HttpExtraHeaders.SIGNATURE_HEADER_NAME, paymentRequestSigner.createRequestSignature(body, authMeans, signer));
        headers.add(HttpExtraHeaders.IDEMPOTENT_KEY, requestIdempotentKeyProvider.provideIdempotentKey(body));
        return headers;
    }

    @Override
    public HttpHeaders createCommonPaymentHttpHeaders(String accessToken,
                                                      DefaultAuthMeans authMeans) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.add(HttpExtraHeaders.FINANCIAL_ID_HEADER_NAME, authMeans.getInstitutionId());
        headers.add(X_LBG_CHANNEL_HEADER_NAME, RC_LBG_CHANNEL);
        return headers;
    }
}
