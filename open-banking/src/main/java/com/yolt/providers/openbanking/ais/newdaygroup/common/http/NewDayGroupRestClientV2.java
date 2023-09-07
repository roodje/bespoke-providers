package com.yolt.providers.openbanking.ais.newdaygroup.common.http;

import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.DefaultRestClient;
import com.yolt.providers.openbanking.ais.generic2.signer.PaymentRequestSigner;
import com.yolt.providers.openbanking.ais.newdaygroup.common.model.NewDayAutoOnboardingResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

public class NewDayGroupRestClientV2 extends DefaultRestClient {

    private static final String APPLICATION_JWT = "application/jwt";

    public NewDayGroupRestClientV2(final PaymentRequestSigner payloadSigner) {
        super(payloadSigner);
    }

    public Optional<NewDayAutoOnboardingResponse> register(final RestTemplate restTemplate,
                                                           final String payload,
                                                           final String providerKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, APPLICATION_JWT);
        HttpEntity<String> httpEntity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<NewDayAutoOnboardingResponse> responseEntity = restTemplate.exchange(
                    "/v3.1/register", HttpMethod.POST, httpEntity, NewDayAutoOnboardingResponse.class);
            return Optional.ofNullable(responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            throw new AutoOnboardingException(providerKey, "Auto-Onboarding failed", e);
        }
    }
}
