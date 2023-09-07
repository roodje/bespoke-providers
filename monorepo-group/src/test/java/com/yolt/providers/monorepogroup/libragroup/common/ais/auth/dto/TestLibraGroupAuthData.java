package com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class TestLibraGroupAuthData implements LibraGroupAuthData {

    private final String accessToken;
    private final Long expiresIn;
    private final String refreshToken;

}
