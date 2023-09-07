package com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TestLibraGroupTokens extends LibraGroupTokens {
    private final String accessToken;
    private final Long expiryTimestamp;
    private final String refreshToken;
}