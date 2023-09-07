package com.yolt.providers.monorepogroup.atruviagroup.common.dto.internal;

public record ObtainUserNameAndPasswordInputStep() implements StepState {

    @Override
    public String selectedRegionalBankCode() {
        //In this step we should receive selected bank in 'FilledInUserSiteFormValues' from S-M
        return null;
    }
}

