package com.yolt.providers.openbanking.ais.generic2.common;

import com.yolt.providers.common.versioning.ProviderVersion;
import lombok.Value;

@Value
public class ProviderIdentification {
    String identifier;
    String displayName;
    ProviderVersion version;
}
