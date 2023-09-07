package com.yolt.providers.monorepogroup.chebancagroup.common.mapper;

import com.yolt.providers.monorepogroup.chebancagroup.common.dto.external.Transaction;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

public interface CheBancaGroupTransactionMapper {
    ProviderTransactionDTO map(Transaction transaction, TransactionStatus status);
}
