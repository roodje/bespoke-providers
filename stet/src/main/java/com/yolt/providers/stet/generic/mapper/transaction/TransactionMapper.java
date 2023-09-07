package com.yolt.providers.stet.generic.mapper.transaction;

import com.yolt.providers.stet.generic.dto.transaction.StetTransactionDTO;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.util.List;

public interface TransactionMapper {

    List<ProviderTransactionDTO> mapToProviderTransactionDTOs(List<StetTransactionDTO> transactions);

    ExtendedTransactionDTO mapToExtendedTransactionDTO(StetTransactionDTO transactions);
}
