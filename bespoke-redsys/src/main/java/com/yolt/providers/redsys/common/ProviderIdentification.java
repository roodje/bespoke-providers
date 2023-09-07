package com.yolt.providers.redsys.common;

import com.yolt.providers.common.versioning.ProviderVersion;
import lombok.Value;

@Value
public class ProviderIdentification {
    String identifier;
    String displayName;
    ProviderVersion version;
}
