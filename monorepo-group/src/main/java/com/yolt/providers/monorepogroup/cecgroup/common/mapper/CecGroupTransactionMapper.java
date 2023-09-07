package com.yolt.providers.monorepogroup.cecgroup.common.mapper;

import com.yolt.providers.monorepogroup.cecgroup.common.domain.dto.data.Transaction;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

public interface CecGroupTransactionMapper {

    ProviderTransactionDTO mapToProviderTransaction(Transaction transaction, TransactionStatus transactionStatus);
}
