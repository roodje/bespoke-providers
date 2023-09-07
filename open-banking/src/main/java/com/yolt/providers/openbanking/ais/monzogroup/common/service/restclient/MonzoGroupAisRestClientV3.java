package com.yolt.providers.openbanking.ais.monzogroup.common.service.restclient;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import com.yolt.providers.openbanking.ais.monzogroup.common.http.MonzoGroupHttpErrorHandlerV2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

public class MonzoGroupAisRestClientV3 extends DefaultRestClient {

    private static final String GET_POTS = "get_pots";

    public MonzoGroupAisRestClientV3(PaymentRequestSigner payloadSigner) {
        super(payloadSigner);
    }

    public <T> T fetchPots(final HttpClient httpClient,
                           final String currentPath,
                           final AccessMeans accessToken,
                           final String institutionId,
                           final Class<T> responseType) throws TokenInvalidException {
        return httpClient.exchange(currentPath,
                HttpMethod.GET,
                new HttpEntity<>(getHeaders(accessToken, institutionId)),
                GET_POTS,
                responseType,
                MonzoGroupHttpErrorHandlerV2.MONZO_GROUP_ERROR_HANDLER).getBody();
    }

    @Override
    protected HttpErrorHandler getErrorHandler() {
        return MonzoGroupHttpErrorHandlerV2.MONZO_GROUP_ERROR_HANDLER;
    }
}