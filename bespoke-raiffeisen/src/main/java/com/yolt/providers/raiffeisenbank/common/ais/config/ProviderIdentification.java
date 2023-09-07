package com.yolt.providers.raiffeisenbank.common.ais.config;

import com.yolt.providers.common.versioning.ProviderVersion;
import lombok.Value;

@Value
public class ProviderIdentification {

    String providerIdentifier;
    String providerDisplayName;
    ProviderVersion providerVersion;
}
