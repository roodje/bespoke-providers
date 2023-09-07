package com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.monorepogroup.atruviagroup.common.authenticationmeans.AtruviaGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.StepState;
import lombok.AccessLevel;
import lombok.Value;
import lombok.With;
import nl.ing.lovebird.providershared.form.FilledInUserSiteFormValues;

import java.util.UUID;

@Value
public class ProcessStepData {
    String providerState;
    @With(AccessLevel.PRIVATE)
    FilledInUserSiteFormValues filledInUserSiteFormValues;
    RestTemplateManager restTemplateManager;
    String psuIpAddress;
    Signer signer;
    AtruviaGroupAuthenticationMeans atruviaGroupAuthenticationMeans;
    UUID userId;
    StepState stepState;

    public String getFormValue(String fieldId) {
        return filledInUserSiteFormValues.get(fieldId);
    }
}
