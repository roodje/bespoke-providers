package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.service;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.internal.FetchDataResult;

public interface RaiffeisenAtGroupDataMappingService {
    DataProviderResponse mapToDateProviderResponse(final FetchDataResult fetchDataResult);
}
