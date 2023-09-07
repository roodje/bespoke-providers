package com.yolt.providers.monorepogroup.handelsbankengroup.common.onboarding;

import com.yolt.providers.monorepogroup.handelsbankengroup.common.auth.HandelsbankenGroupAuthMeans;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.http.HandelsbankenGroupHttpClient;

public class HandelsbankenGroupOnboardingServiceV1 implements HandelsbankenGroupOnboardingService {

    private static final String ACCOUNTS_PRODUCT_NAME = "accounts";
    private static final String CARD_ACCOUNTS_PRODUCT_NAME = "card-accounts";

    @Override
    public RegistrationData registerAndSubscribe(HandelsbankenGroupHttpClient httpClient,
                                                 HandelsbankenGroupAuthMeans authMeans,
                                                 String redirectUri,
                                                 String providerDisplayName) {
        try {

            String tppId = httpClient.registerThirdParty()
                    .getTppId();

            String ccgToken = httpClient.doClientCredentialsGrant(tppId)
                    .getAccessToken();

            String clientId = httpClient.register(ccgToken, tppId, authMeans.getAppName(),
                    authMeans.getAppDescription(), redirectUri);

            httpClient.subscribeProduct(ccgToken, tppId, clientId, ACCOUNTS_PRODUCT_NAME);
            httpClient.subscribeProduct(ccgToken, tppId, clientId, CARD_ACCOUNTS_PRODUCT_NAME);

            return new RegistrationData(tppId, clientId);
        } catch (Exception e) {
            throw new RuntimeException("Autoonboarding failed");
        }
    }
}
