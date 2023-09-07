package com.yolt.providers.argentagroup.common.service.consent;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InitiateConsentResult {

    private final String authorizationUrl;
    private final String consentId;
}
