package com.yolt.providers.kbcgroup.common.dto;

import lombok.Data;

@Data
public class KbcGroupAccessMeans {
    private final KbcGroupTokenResponse kbcGroupTokenResponse;
    private final String redirectUrl;
    private final String consentId;
}
