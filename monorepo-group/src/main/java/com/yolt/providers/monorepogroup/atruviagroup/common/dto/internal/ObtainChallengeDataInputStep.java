package com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal;

public record ObtainChallengeDataInputStep(String selectedRegionalBankCode, String username, String consentId,
                                    String authorisationId) implements StepState {
}
