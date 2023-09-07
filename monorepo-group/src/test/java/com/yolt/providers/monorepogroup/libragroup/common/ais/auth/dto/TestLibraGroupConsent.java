package com.yolt.providers.monorepogroup.libragroup.common.ais.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class TestLibraGroupConsent implements LibraGroupConsent {

    private final String consentId;

}
