package com.yolt.providers.redsys.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;

import java.time.Instant;

/**
 * Custom Redsys access means, it contains additional fields:
 * consentId - required when fetching data
 * redirectUrl - redirect URL for Yolt, required for token refresh
 */
@Data
@AllArgsConstructor
public class RedsysAccessMeans {

    private Token token;
    private String redirectUrl;
    private String consentId;
    private String codeVerifier;
    private Instant consentAt;
    private FilledInUserSiteFormValues formValues;

    public RedsysAccessMeans(String codeVerifier) {
        this.codeVerifier = codeVerifier;
    }

    public RedsysAccessMeans(String codeVerifier, FilledInUserSiteFormValues formValues) {
        this.codeVerifier = codeVerifier;
        this.formValues = formValues;
    }
}
