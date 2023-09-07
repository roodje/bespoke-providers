package com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal;

public record ObtainChallengeOutcomeInputStep(String selectedRegionalBankCode, String username, String consentId,
                                              String authorisationId,
                                              AtruviaFormDecryptor atruviaEncryptionData) implements StepState {
}
