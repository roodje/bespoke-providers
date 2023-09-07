package com.yolt.providers.monorepogroup.olbgroup.common.domain;

import com.yolt.providers.common.versioning.ProviderVersion;
import lombok.Value;

@Value
public class ProviderIdentification {
    private String providerIdentifier;
    private String providerDisplayName;
    private ProviderVersion version;
}
