package com.yolt.providers.fabric.common.onboarding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class FabricGroupOnboardingRequest {
    Boolean aisp;
    Boolean piisp;
    Boolean pisp;
}
