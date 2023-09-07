package com.yolt.providers.monorepogroup.handelsbankengroup.common.http;

import com.yolt.providers.common.rest.http.DefaultHttpClientV3;
import com.yolt.providers.common.rest.http.HttpErrorHandlerV2;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.domain.dto.*;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class HandelsbankenGroupHttpClientV1 extends DefaultHttpClientV3 implements HandelsbankenGroupHttpClient {

    private static final String THIRD_PARTIES_ENDPOINT = "/openbanking/psd2/v1/third-parties";
    private static final String TOKEN_ENDPOINT = "/bb/gls5/oauth2/token/1.0";
    private static final String REGISTRATION_AND_SUBSCRIPTION_ENDPOINT = "/openbanking/psd2/v1/subscriptions";
    private static final String CONSENT_ENDPOINT = "/openbanking/psd2/v1/consents";

    private final HandelsbankenGroupHttpHeadersProducer headersProducer;
    private final HandelsbankenGroupHttpBodyProducer bodyProducer;
    private final HttpErrorHandlerV2 errorHandler;

    HandelsbankenGroupHttpClientV1(MeterRegistry meterRegistry,
                                   RestTemplate restTemplate,
                                   String providerDisplayName,
                                   HandelsbankenGroupHttpHeadersProducer headersProducer,
                                   HandelsbankenGroupHttpBodyProducer bodyProducer,
                                   HttpErrorHandlerV2 errorHandler) {
        super(meterRegistry, restTemplate, providerDisplayName);
        this.headersProducer = headersProducer;
        this.bodyProducer = bodyProducer;
        this.errorHandler = errorHandler;
    }

    @SneakyThrows
    @Override
    public ThirdPartiesResponse registerThirdParty() {

        HttpHeaders headers = headersProducer.thirdPartyHeaders();

        return exchange(
                THIRD_PARTIES_ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(null, headers),
                "third_party_registration",
                ThirdPartiesResponse.class,
                errorHandler).getBody();
    }

    @SneakyThrows
    @Override
    public TokenResponse doClientCredentialsGrant(String tppId) {

        MultiValueMap<String, Object> requestPayload = bodyProducer.clientCredentialsGrantPayload(tppId);
        HttpHeaders headers = headersProducer.tokenHeaders();

        return exchange(TOKEN_ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(requestPayload, headers),
                ProviderClientEndpoints.CLIENT_CREDENTIALS_GRANT,
                TokenResponse.class,
                errorHandler).getBody();
    }

    @SneakyThrows
    @Override
    public String register(String ccgToken,
                           String tppId,
                           String appName,
                           String appDescription,
                           String redirectUri) {

        RegistrationRequest registrationPayload = bodyProducer.registrationPayload(appName, appDescription, redirectUri);
        HttpHeaders headers = headersProducer.registrationAndSubscriptionHeaders(tppId, ccgToken);

        return exchange(REGISTRATION_AND_SUBSCRIPTION_ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(registrationPayload, headers),
                ProviderClientEndpoints.REGISTER,
                RegistrationAndSubscriptionResponse.class,
                errorHandler).getBody().getClientId();
    }

    @SneakyThrows
    @Override
    public void subscribeProduct(String ccgToken, String tppId, String clientId, String productName) {
        SubscriptionRequest subscriptionPayload = bodyProducer.subscriptionPayload(clientId, productName);
        HttpHeaders headers = headersProducer.registrationAndSubscriptionHeaders(tppId, ccgToken);

        exchange(REGISTRATION_AND_SUBSCRIPTION_ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(subscriptionPayload, headers),
                "subscribe",
                RegistrationAndSubscriptionResponse.class,
                errorHandler);
    }

    @SneakyThrows
    @Override
    public ConsentResponse createConsent(String clientId, String ccgToken) {

        ConsentRequest consentPayload = bodyProducer.createConsentPayload();
        HttpHeaders headers = headersProducer.createConsentHeaders(clientId, ccgToken);

        return exchange(CONSENT_ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(consentPayload, headers),
                ProviderClientEndpoints.GET_ACCOUNT_ACCESS_CONSENT,
                ConsentResponse.class,
                errorHandler).getBody();
    }

    @SneakyThrows
    @Override
    public TokenResponse createAccessToken(String clientId, String consentId, String authCode, String redirectUri) {
        MultiValueMap<String, Object> accessTokenPayload = bodyProducer.accessTokenPayload(clientId, consentId, authCode, redirectUri);
        HttpHeaders headers = headersProducer.tokenHeaders();

        return exchange(TOKEN_ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(accessTokenPayload, headers),
                ProviderClientEndpoints.GET_ACCESS_TOKEN,
                TokenResponse.class,
                errorHandler).getBody();
    }

    @SneakyThrows
    @Override
    public TokenResponse refreshAccessToken(String clientId, String refreshToken) {
        MultiValueMap<String, Object> refreshTokenPayload = bodyProducer.refreshTokenPayload(clientId, refreshToken);
        HttpHeaders headers = headersProducer.tokenHeaders();

        return exchange(TOKEN_ENDPOINT,
                HttpMethod.POST,
                new HttpEntity<>(refreshTokenPayload, headers),
                ProviderClientEndpoints.REFRESH_TOKEN,
                TokenResponse.class,
                errorHandler).getBody();
    }
}
