package com.yolt.providers.sparkassenandlandesbanks.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConsentUrlData {
    private String consentUrl;
    private String consentId;
    private String codeVerifier;
    private String wellKnownEndpoint;
}
