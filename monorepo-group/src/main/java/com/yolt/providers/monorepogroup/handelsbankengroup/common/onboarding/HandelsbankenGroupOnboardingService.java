package com.yolt.providers.monorepogroup.handelsbankengroup.common.onboarding;

import com.yolt.providers.monorepogroup.handelsbankengroup.common.auth.HandelsbankenGroupAuthMeans;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.http.HandelsbankenGroupHttpClient;

public interface HandelsbankenGroupOnboardingService {

    RegistrationData registerAndSubscribe(HandelsbankenGroupHttpClient httpClient,
                                          HandelsbankenGroupAuthMeans authMeans,
                                          String redirectUri,
                                          String providerDisplayName);

    record RegistrationData(String tppId, String clientId) {
    }
}
