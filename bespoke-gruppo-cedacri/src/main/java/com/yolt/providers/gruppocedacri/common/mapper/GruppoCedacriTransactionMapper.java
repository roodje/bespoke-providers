package com.yolt.providers.gruppocedacri.common.mapper;

import com.yolt.providers.gruppocedacri.common.dto.fetchdata.Transaction;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

public interface GruppoCedacriTransactionMapper {

    ProviderTransactionDTO map(Transaction transaction, TransactionStatus status);
}
