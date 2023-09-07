package com.yolt.providers.stet.generic.domain;

import com.yolt.providers.common.versioning.ProviderVersion;
import lombok.Value;

@Value
public class ProviderIdentification {

    String identifier;
    String displayName;
    ProviderVersion version;
}
