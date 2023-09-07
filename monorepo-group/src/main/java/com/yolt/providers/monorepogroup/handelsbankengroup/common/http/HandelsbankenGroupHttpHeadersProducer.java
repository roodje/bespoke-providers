package com.yolt.providers.monorepogroup.handelsbankengroup.common.http;

import org.springframework.http.HttpHeaders;

public interface HandelsbankenGroupHttpHeadersProducer {
    HttpHeaders thirdPartyHeaders();

    HttpHeaders tokenHeaders();

    HttpHeaders registrationAndSubscriptionHeaders(String tppId, String ccgToken);

    HttpHeaders createConsentHeaders(String clientId, String ccgToken);
}
