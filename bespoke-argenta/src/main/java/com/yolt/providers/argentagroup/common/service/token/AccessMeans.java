package com.yolt.providers.argentagroup.common.service.token;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccessMeans {

    private final String accessToken;
    private final String refreshToken;
    private final String scope;
    private final String consentId;
    private final Long expiresIn;
}
