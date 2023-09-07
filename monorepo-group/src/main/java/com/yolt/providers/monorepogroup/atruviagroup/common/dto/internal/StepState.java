package com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        property = "embeddedFlowStep") @JsonSubTypes({

        @JsonSubTypes.Type(value = ObtainUserNameAndPasswordInputStep.class, name = "OBTAIN_USERNAME_PASSWORD"),
        @JsonSubTypes.Type(value = CreateConsentAndObtainScaMethodInputStep.class, name = "OBTAIN_SCA_METHOD"),
        @JsonSubTypes.Type(value = ObtainChallengeDataInputStep.class, name = "OBTAIN_CHALLENGE_DATA"),
        @JsonSubTypes.Type(value = ObtainChallengeOutcomeInputStep.class, name = "OBTAIN_CHALLENGE_OUTCOME")
})
public interface StepState {

    String selectedRegionalBankCode();
}
