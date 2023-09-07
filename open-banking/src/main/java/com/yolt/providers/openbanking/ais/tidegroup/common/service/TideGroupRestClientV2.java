package com.yolt.providers.openbanking.ais.tidegroup.common.service;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.generic2.dto.AutoOnboardingResponse;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpErrorHandler;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.Collections;

import static com.yolt.providers.openbanking.ais.tidegroup.common.TideHttpErrorHandler.TIDE_HTTP_ERROR_HANDLER;

public class TideGroupRestClientV2 extends DefaultRestClient {

    private static final String CONTENT_TYPE = "application/jwt";

    public TideGroupRestClientV2(PaymentRequestSigner payloadSigner) {
        super(payloadSigner);
    }

    public AutoOnboardingResponse register(HttpClient httpClient,
                                           String payload,
                                           String registrationUrl) throws TokenInvalidException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE);
        HttpEntity<String> httpEntity = new HttpEntity<>(payload, headers);
        return httpClient.exchange(registrationUrl, HttpMethod.POST, httpEntity, ProviderClientEndpoints.REGISTER, AutoOnboardingResponse.class).getBody();
    }

    @Override
    protected HttpErrorHandler getErrorHandler() {
        return TIDE_HTTP_ERROR_HANDLER;
    }
}
