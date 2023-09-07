package com.yolt.providers.kbcgroup.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConsentUrlData {
    private String consentUrl;
    private String codeVerifier;
}
