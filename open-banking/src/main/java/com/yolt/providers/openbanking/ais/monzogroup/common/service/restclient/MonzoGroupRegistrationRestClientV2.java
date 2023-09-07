package com.yolt.providers.openbanking.ais.monzogroup.common.service.restclient;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.generic2.dto.AutoOnboardingResponse;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.Collections;

public class MonzoGroupRegistrationRestClientV2 extends DefaultRestClient {

    public MonzoGroupRegistrationRestClientV2(PaymentRequestSigner payloadSigner) {
        super(payloadSigner);
    }

    public AutoOnboardingResponse register(HttpClient httpClient,
                                           String payload,
                                           String registrationUrl) throws TokenInvalidException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.CONTENT_TYPE, "application/jwt");
        HttpEntity<String> httpEntity = new HttpEntity<>(payload, headers);
        return httpClient.exchange(registrationUrl, HttpMethod.POST, httpEntity, ProviderClientEndpoints.REGISTER, AutoOnboardingResponse.class).getBody();
    }
}
