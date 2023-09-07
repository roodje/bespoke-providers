package com.yolt.providers.monorepogroup.qontogroup.common.service;

import com.yolt.providers.monorepogroup.qontogroup.common.dto.internal.QontoFetchDataResult;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;

import java.util.List;

public interface QontoGroupMappingService {

    List<ProviderAccountDTO> mapToListOfProviderAccountDto(QontoFetchDataResult fetchDataResult);
}
