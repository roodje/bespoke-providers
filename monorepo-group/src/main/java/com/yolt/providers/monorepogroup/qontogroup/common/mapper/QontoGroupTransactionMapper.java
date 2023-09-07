package com.yolt.providers.monorepogroup.qontogroup.common.mapper;

import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Transaction;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

public interface QontoGroupTransactionMapper {

    ProviderTransactionDTO map(Transaction transaction);
}
