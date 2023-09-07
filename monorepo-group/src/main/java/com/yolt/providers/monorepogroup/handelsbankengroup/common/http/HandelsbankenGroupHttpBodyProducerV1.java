package com.yolt.providers.monorepogroup.handelsbankengroup.common.http;

import com.yolt.providers.monorepogroup.handelsbankengroup.common.domain.dto.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.yolt.providers.common.constants.OAuth.*;

public class HandelsbankenGroupHttpBodyProducerV1 implements HandelsbankenGroupHttpBodyProducer {

    private static final String CCG_ADMIN_SCOPE = "PSD2-ADMIN";
    private static final String ALL_ACCOUNTS_ACCESS = "ALL_ACCOUNTS";

    @Override
    public MultiValueMap<String, Object> clientCredentialsGrantPayload(String clientId) {
        MultiValueMap<String, Object> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(GRANT_TYPE, CLIENT_CREDENTIALS);
        requestPayload.add(SCOPE, CCG_ADMIN_SCOPE);
        requestPayload.add(CLIENT_ID, clientId);
        return requestPayload;
    }

    @Override
    public RegistrationRequest registrationPayload(String appName, String appDescription, String redirectUri) {
        return RegistrationRequest.builder()
                .app(App.builder()
                        .name(appName)
                        .description(appDescription)
                        .oauthRedirectURI(redirectUri)
                        .build())
                .product(Product.builder()
                        .name("consents")
                        .build())
                .build();
    }

    @Override
    public SubscriptionRequest subscriptionPayload(String clientId, String productName) {

        return SubscriptionRequest.builder()
                .clientId(clientId)
                .product(Product.builder()
                        .name(productName)
                        .build())
                .build();
    }

    @Override
    public ConsentRequest createConsentPayload() {
        return ConsentRequest.builder()
                .access(ALL_ACCOUNTS_ACCESS)
                .build();
    }

    @Override
    public MultiValueMap<String, Object> accessTokenPayload(String clientId, String consentId, String authCode, String redirectUri) {
        MultiValueMap<String, Object> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(GRANT_TYPE, AUTHORIZATION_CODE);
        requestPayload.add(CODE, authCode);
        requestPayload.add(CLIENT_ID, clientId);
        requestPayload.add(REDIRECT_URI, redirectUri);
        requestPayload.add(SCOPE, String.format("AIS:%s", consentId));
        return requestPayload;
    }

    @Override
    public MultiValueMap<String, Object> refreshTokenPayload(String clientId, String refreshToken) {
        MultiValueMap<String, Object> requestPayload = new LinkedMultiValueMap<>();
        requestPayload.add(GRANT_TYPE, REFRESH_TOKEN);
        requestPayload.add(REFRESH_TOKEN, refreshToken);
        requestPayload.add(CLIENT_ID, clientId);
        return requestPayload;
    }
}
