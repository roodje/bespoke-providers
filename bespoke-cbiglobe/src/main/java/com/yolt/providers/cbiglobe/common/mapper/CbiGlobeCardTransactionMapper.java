package com.yolt.providers.cbiglobe.common.mapper;

import com.yolt.providers.cbiglobe.dto.ReadCardAccountTransactionListResponseTypeTransactions;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.util.List;

public interface CbiGlobeCardTransactionMapper {
    List<ProviderTransactionDTO> mapToProviderTransactionDTOs(ReadCardAccountTransactionListResponseTypeTransactions transactions);
}
