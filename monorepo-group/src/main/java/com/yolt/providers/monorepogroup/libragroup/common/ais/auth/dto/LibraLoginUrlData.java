package com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Getter
@RequiredArgsConstructor
@Value
public class LibraLoginUrlData {
    private final String loginUrl;
    private final String consentId;
}
