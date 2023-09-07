package com.yolt.providers.stet.lclgroup.common.onboarding;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.lclgroup.common.auth.LclGroupClientConfiguration;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpMethod.GET;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LclRegistrationHttpClient {

    private final RestTemplate restTemplate;
    private final String registrationUrl;

    public static LclRegistrationHttpClient createHttpClient(RestTemplateManager restTemplateManager,
                                                             LclGroupClientConfiguration clientConfiguration,
                                                             DefaultProperties properties) {
        return new LclRegistrationHttpClient(LclGroupHttpUtil
                .createRestTemplateWithManagedMutualTLSTemplate(restTemplateManager, clientConfiguration, properties.getRegions().get(0).getBaseUrl()),
                properties.getRegistrationUrl());
    }

    public LclGroupClientRegistration createRegistration(LclRegistrationRequest registrationRequest,
                                                         SignatureData signatureData) {
        return exchangeForRegistration(registrationUrl, HttpMethod.POST, registrationRequest, signatureData, LclGroupClientRegistration.class);
    }

    public LclRegistrationResponse readRegistration(String clientId,
                                                    SignatureData signatureData) {
        return exchangeForRegistration(registrationUrl + "/" + clientId, GET, null, signatureData, LclRegistrationResponse.class);
    }

    public void updateRegistration(String clientId,
                                   LclRegistrationUpdateRequest registrationUpdateRequest,
                                   SignatureData signatureData) {
        exchangeForRegistration(registrationUrl + "/" + clientId, HttpMethod.PUT, registrationUpdateRequest, signatureData, LclRegistrationResponse.class);
    }

    private <T> T exchangeForRegistration(String url,
                                          HttpMethod httpMethod,
                                          Object registrationRequest,
                                          SignatureData signatureData,
                                          Class<T> responseType) {
        HttpHeaders headers = LclGroupSigningUtil.getRegistrationHeaders(
                url.substring(url.indexOf("/register")),
                httpMethod,
                new HttpHeaders(),
                registrationRequest,
                signatureData);
        try {
            return restTemplate
                    .exchange(url, httpMethod, new HttpEntity<>(registrationRequest, headers), responseType).getBody();

        } catch (HttpStatusCodeException e) {
            throw new LclGroupAutoOnBoardingException(e);
        }
    }

}
