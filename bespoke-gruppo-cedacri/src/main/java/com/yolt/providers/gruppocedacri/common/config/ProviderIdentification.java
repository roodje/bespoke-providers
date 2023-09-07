package com.yolt.providers.gruppocedacri.common.config;

import com.yolt.providers.common.versioning.ProviderVersion;
import lombok.Value;

@Value
public class ProviderIdentification {

    String providerIdentifier;
    String providerDisplayName;
    ProviderVersion providerVersion;
}
