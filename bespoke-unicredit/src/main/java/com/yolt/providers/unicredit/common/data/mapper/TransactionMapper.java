package com.yolt.providers.unicredit.common.data.mapper;

import com.yolt.providers.unicredit.common.dto.UniCreditTransactionsDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.util.List;

public interface TransactionMapper {
    List<ProviderTransactionDTO> mapTransactions(final List<UniCreditTransactionsDTO> transactionsDTOs);
}
