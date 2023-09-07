package com.yolt.providers.openbanking.ais.sainsburys.service.ais.restclient;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.dto.AutoOnboardingResponse;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import org.springframework.http.*;

import java.util.Collections;

public class SainsburysRestClientV2 extends DefaultRestClient {

    public SainsburysRestClientV2(PaymentRequestSigner payloadSigner) {
        super(payloadSigner);
    }

    public AutoOnboardingResponse register(final HttpClient httpClient,
                                           final String payload,
                                           final String registrationUrl) throws TokenInvalidException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        headers.set(HttpHeaders.CONTENT_TYPE, "application/jwt");
        HttpEntity<String> httpEntity = new HttpEntity<>(payload, headers);
        return httpClient.exchange(registrationUrl, HttpMethod.POST, httpEntity, ProviderClientEndpoints.REGISTER, AutoOnboardingResponse.class).getBody();
    }

    public ResponseEntity<Void> removeRegistration(final HttpClient httpClient,
                                                   final String registrationRemovalUrl,
                                                   final String clientId,
                                                   final AccessMeans clientAccessToken) throws TokenInvalidException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(clientAccessToken.getAccessToken());
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);
        return httpClient.exchange(registrationRemovalUrl, HttpMethod.DELETE, httpEntity, ProviderClientEndpoints.DELETE_REGISTRATION, Void.class, clientId);
    }
}
