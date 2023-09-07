package com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal;

public record CreateConsentAndObtainScaMethodInputStep(String selectedRegionalBankCode,
                                                AtruviaFormDecryptor atruviaEncryptionData) implements StepState {

}
