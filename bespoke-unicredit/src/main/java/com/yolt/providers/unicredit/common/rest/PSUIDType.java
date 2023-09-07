package com.yolt.providers.unicredit.common.rest;

import java.util.Arrays;
import java.util.List;

public enum PSUIDType {
    ALL("UNICREDIT", "UNICREDIT_RO"),
    HVB_ONLINEBANKING("HYPOVEREINSBANK");

    private List<String> supportingProviders;

    PSUIDType(String... supportingProviders) {
        this.supportingProviders = Arrays.asList(supportingProviders);
    }

    static PSUIDType forProvider(final String provider) {
        for (PSUIDType psuidType : PSUIDType.values()) {
            if (psuidType.supportingProviders.contains(provider)) {
                return psuidType;
            }
        }
        return ALL;
    }
}
