package com.yolt.providers.volksbank.common.model;

import lombok.Data;

/**
 * Custom Volksbank access means, it contains additional fields:
 * consentId - required when fetching data
 * redirectUrl - redirect URL for Yolt, required for token refresh
 */
@Data
public class VolksbankAccessMeans {

    private final VolksbankAccessTokenResponse response;
    private final String redirectUrl;
    private final String consentId;
}
