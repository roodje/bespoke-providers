package com.yolt.providers.monorepogroup.cecgroup.common;

import lombok.Value;

import java.io.Serializable;

@Value
public class CecGroupAccessMeans implements Serializable {
    String consentId;
    String accessToken;
    String refreshToken;
    Long expirationTimestamp;
}
