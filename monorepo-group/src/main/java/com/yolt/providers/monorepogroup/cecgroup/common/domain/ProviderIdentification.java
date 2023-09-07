package com.yolt.providers.monorepogroup.cecgroup.common.domain;

import com.yolt.providers.common.versioning.ProviderVersion;
import lombok.Value;

@Value
public class ProviderIdentification {
    String providerIdentifier;
    String providerDisplayName;
    ProviderVersion version;
}
