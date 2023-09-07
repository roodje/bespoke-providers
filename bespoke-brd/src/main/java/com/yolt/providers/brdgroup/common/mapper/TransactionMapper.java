package com.yolt.providers.brdgroup.common.mapper;

import com.yolt.providers.brdgroup.common.dto.fetchdata.Transaction;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

public interface TransactionMapper {

    ProviderTransactionDTO map(Transaction transaction, TransactionStatus status);
}
