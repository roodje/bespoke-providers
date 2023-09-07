package com.yolt.providers.monorepogroup.qontogroup.common.mapper.auth;

import com.yolt.providers.monorepogroup.qontogroup.common.dto.internal.QontoGroupProviderState;

public interface QontoGroupProviderStateMapper {

    String serialize(final QontoGroupProviderState providerState) throws ProviderStateProcessingException;

    QontoGroupProviderState deserialize(final String serializedProviderState) throws ProviderStateProcessingException;
}
