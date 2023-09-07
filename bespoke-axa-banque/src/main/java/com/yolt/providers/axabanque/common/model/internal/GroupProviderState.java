package com.yolt.providers.axabanque.common.model.internal;

import lombok.Value;


@Value
public class GroupProviderState {
    String codeVerifier;
    String code;
    String consentId;
    String traceId;
    Long consentGeneratedAt;
}
