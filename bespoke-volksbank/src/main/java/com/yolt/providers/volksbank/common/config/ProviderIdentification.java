package com.yolt.providers.volksbank.common.config;

import com.yolt.providers.common.versioning.ProviderVersion;
import lombok.Value;

@Value
public class ProviderIdentification {

    private String providerIdentifier;
    private String providerDisplayName;
    private ProviderVersion providerVersion;
}
