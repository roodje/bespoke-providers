package com.yolt.providers.deutschebank.common.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeutscheBankGroupProviderState {

    private String consentId;

    public DeutscheBankGroupProviderState(String consentId) {
        this.consentId = consentId;
    }
}
