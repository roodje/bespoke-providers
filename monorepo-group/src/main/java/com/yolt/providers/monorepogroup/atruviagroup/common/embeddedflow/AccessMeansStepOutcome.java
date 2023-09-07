package com.yolt.providers.monorepogroup.atruviagroup.common.embeddedflow;

import com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal.AtruviaAccessMeans;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.UUID;

@Value
@EqualsAndHashCode(callSuper = true)
public class AccessMeansStepOutcome extends StepOutcome {
    AtruviaAccessMeans atruviaAccessMeans;
    UUID userId;
    long consentValidity;
}
