package com.yolt.providers.axabanque.common.fetchdata.mapper;

import com.yolt.providers.axabanque.common.model.external.Transactions;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.util.List;

public interface TransactionMapper {
    List<ProviderTransactionDTO> mapToTransactions(List<Transactions> transaction);
}
