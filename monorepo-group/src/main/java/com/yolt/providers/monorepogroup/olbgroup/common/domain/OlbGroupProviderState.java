package com.yolt.providers.monorepogroup.olbgroup.common.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OlbGroupProviderState {

    private String consentId;

    public OlbGroupProviderState(String consentId) {
        this.consentId = consentId;
    }
}
