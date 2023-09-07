package com.yolt.providers.monorepogroup.handelsbankengroup.common.http;

import com.yolt.providers.monorepogroup.handelsbankengroup.common.domain.dto.ConsentResponse;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.domain.dto.ThirdPartiesResponse;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.domain.dto.TokenResponse;

public interface HandelsbankenGroupHttpClient {

    ThirdPartiesResponse registerThirdParty();

    TokenResponse doClientCredentialsGrant(String tppId);

    String register(String ccgToken, String tppId, String appName, String appDescription, String redirectUri);

    void subscribeProduct(String ccgToken, String tppId, String clientId, String productName);

    ConsentResponse createConsent(String clientId, String ccgToken);

    TokenResponse createAccessToken(String clientId, String consentId, String authCode, String redirectUri);

    TokenResponse refreshAccessToken(String clientId, String refreshToken);
}
