package com.yolt.providers.cbiglobe.common.mapper;

import com.yolt.providers.cbiglobe.dto.TransactionsReadaccounttransactionlistType1;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.util.List;

public interface CbiGlobeTransactionMapper {
    List<ProviderTransactionDTO> mapToProviderTransactionDTOs(TransactionsReadaccounttransactionlistType1 transactions);
}
