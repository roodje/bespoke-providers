package com.yolt.providers.fabric.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthorizationConsentResourceRequest {
    private String consentId;
    private String scaStatus;
}
