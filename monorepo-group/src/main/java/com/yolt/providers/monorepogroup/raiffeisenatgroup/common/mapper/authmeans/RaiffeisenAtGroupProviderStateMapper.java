package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.authmeans;

import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.internal.RaiffeisenAtGroupProviderState;

public interface RaiffeisenAtGroupProviderStateMapper {

    String serialize(final RaiffeisenAtGroupProviderState providerState) throws ProviderStateProcessingException;

    RaiffeisenAtGroupProviderState deserialize(final String providerState) throws ProviderStateProcessingException;
}
