package com.yolt.providers.openbanking.ais.nationwide.service.restclient;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Collections;

public class NationwideRestClientPisV8 extends DefaultRestClient {

    public NationwideRestClientPisV8(PaymentRequestSigner payloadSigner) {
        super(payloadSigner);
    }

    @Override
    protected HttpHeaders getPaymentHttpHeaders(final AccessMeans clientAccessToken,
                                                final DefaultAuthMeans authMeans,
                                                final Object requestBody,
                                                final Signer signer) {
        HttpHeaders headers = super.getPaymentHttpHeaders(clientAccessToken, authMeans, requestBody, signer);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    @Override
    protected HttpHeaders getHeaders(final AccessMeans clientAccessToken,
                                     final String institutionId) {
        HttpHeaders headers = super.getHeaders(clientAccessToken, institutionId);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        return headers;
    }

}
