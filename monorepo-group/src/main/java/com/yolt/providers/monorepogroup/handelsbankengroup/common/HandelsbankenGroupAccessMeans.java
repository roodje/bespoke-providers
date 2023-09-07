package com.yolt.providers.monorepogroup.handelsbankengroup.common;

import lombok.Value;

@Value
public class HandelsbankenGroupAccessMeans {

    String consentId;
    String accessToken;
    String refreshToken;
    Long expirationTimestamp;
}
