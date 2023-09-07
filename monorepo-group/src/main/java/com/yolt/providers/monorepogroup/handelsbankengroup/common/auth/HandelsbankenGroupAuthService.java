package com.yolt.providers.monorepogroup.handelsbankengroup.common.auth;

import com.yolt.providers.monorepogroup.handelsbankengroup.common.HandelsbankenGroupAccessMeans;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.domain.ConsentData;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.http.HandelsbankenGroupHttpClient;

public interface HandelsbankenGroupAuthService {

    ConsentData generateConsent(HandelsbankenGroupHttpClient httpClient,
                                String tppId,
                                String clientId,
                                String redirectUri,
                                String state);

    HandelsbankenGroupAccessMeans createAccessMeans(HandelsbankenGroupHttpClient httpClient,
                                                    String clientId,
                                                    String consentId,
                                                    String authCode,
                                                    String baseClientRedirectUrl);

    HandelsbankenGroupAccessMeans refreshAccessMeans(HandelsbankenGroupHttpClient httpClient,
                                                     String clientId,
                                                     HandelsbankenGroupAccessMeans oldAccessMeans);
}
