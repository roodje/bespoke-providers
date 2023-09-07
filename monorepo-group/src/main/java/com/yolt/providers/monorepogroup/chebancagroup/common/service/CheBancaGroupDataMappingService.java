package com.yolt.providers.monorepogroup.chebancagroup.common.service;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.internal.FetchDataResult;

public interface CheBancaGroupDataMappingService {
    DataProviderResponse mapToDateProviderResponse(final FetchDataResult fetchDataResult) throws ProviderFetchDataException;
}
