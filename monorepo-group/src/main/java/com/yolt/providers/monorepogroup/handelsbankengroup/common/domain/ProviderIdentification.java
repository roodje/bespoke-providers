package com.yolt.providers.monorepogroup.handelsbankengroup.common.domain;

import com.yolt.providers.common.versioning.ProviderVersion;

public record ProviderIdentification(String providerIdentifier,
                                     String providerDisplayName,
                                     ProviderVersion version) {
}
