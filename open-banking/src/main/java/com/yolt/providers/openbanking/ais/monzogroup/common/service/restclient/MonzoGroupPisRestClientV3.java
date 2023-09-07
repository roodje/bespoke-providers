package com.yolt.providers.openbanking.ais.monzogroup.common.service.restclient;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import com.yolt.providers.openbanking.ais.monzogroup.common.http.MonzoGroupHttpErrorHandlerV2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

public class MonzoGroupPisRestClientV3 extends DefaultRestClient {

    public MonzoGroupPisRestClientV3(PaymentRequestSigner payloadSigner) {
        super(payloadSigner);
    }

    @Override
    public <T> T createPayment(final HttpClient httpClient,
                               final String exchangePath,
                               final AccessMeans clientAccessToken,
                               final DefaultAuthMeans authMeans,
                               final Object requestBody,
                               final Class<T> responseType,
                               final Signer signer) throws TokenInvalidException {
        HttpHeaders headers = getPaymentHttpHeaders(clientAccessToken, authMeans, requestBody, signer);

        return httpClient.exchange(exchangePath,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                ProviderClientEndpoints.INITIATE_PAYMENT,
                responseType,
                MonzoGroupHttpErrorHandlerV2.MONZO_GROUP_ERROR_HANDLER).getBody();
    }
}