package com.yolt.providers.monorepogroup.handelsbankengroup.common.http;

import com.yolt.providers.monorepogroup.handelsbankengroup.common.domain.dto.ConsentRequest;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.domain.dto.RegistrationRequest;
import com.yolt.providers.monorepogroup.handelsbankengroup.common.domain.dto.SubscriptionRequest;
import org.springframework.util.MultiValueMap;

public interface HandelsbankenGroupHttpBodyProducer {

    MultiValueMap<String, Object> clientCredentialsGrantPayload(String clientId);

    RegistrationRequest registrationPayload(String appName, String appDescription, String redirectUri);

    SubscriptionRequest subscriptionPayload(String clientId, String productName);

    ConsentRequest createConsentPayload();

    MultiValueMap<String, Object> accessTokenPayload(String clientId, String consentId, String authCode, String redirectUri);

    MultiValueMap<String, Object> refreshTokenPayload(String clientId, String refreshToken);
}
