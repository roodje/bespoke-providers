package com.yolt.providers.fabric.common.mapper;

import com.yolt.providers.fabric.common.model.Transactions;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.util.List;

public interface TransactionMapper {
    List<ProviderTransactionDTO> mapToTransactions(List<Transactions> transaction);
}
