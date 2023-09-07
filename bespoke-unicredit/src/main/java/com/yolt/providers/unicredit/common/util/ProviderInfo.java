package com.yolt.providers.unicredit.common.util;

import com.yolt.providers.common.versioning.ProviderVersion;
import lombok.Value;

@Value
public class ProviderInfo {
    private String identifier;
    private String displayName;
    private ProviderVersion version;
}
