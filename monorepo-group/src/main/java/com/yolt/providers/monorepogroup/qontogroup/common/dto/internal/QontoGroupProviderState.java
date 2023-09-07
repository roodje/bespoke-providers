package com.yolt.providers.monorepogroup.qontogroup.common.dto.internal;

import lombok.Value;

@Value
public class QontoGroupProviderState {

    private String accessToken;
    private String refreshToken;
    private Long expirationTimeInMillis;
}
