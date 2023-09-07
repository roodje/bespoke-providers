package com.yolt.providers.monorepogroup.chebancagroup.common.dto;

import lombok.Value;

@Value
public class ChaBancaGroupAccessMeans {
    String accessToken;
    String tokenType;
    Long tokenValidityTimeInSeconds;
    String refreshToken;
    String refreshTokenValidityTimeInSeconds;
    String scope;
}