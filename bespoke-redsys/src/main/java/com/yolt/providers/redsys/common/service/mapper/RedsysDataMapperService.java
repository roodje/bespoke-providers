package com.yolt.providers.redsys.common.service.mapper;

import com.yolt.providers.redsys.common.dto.AccountDetails;
import com.yolt.providers.redsys.common.dto.Balance;
import com.yolt.providers.redsys.common.dto.Transaction;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.util.List;

public interface RedsysDataMapperService {
    ProviderAccountDTO toProviderAccountDTO(AccountDetails account,
                                            List<Balance> accountBalances,
                                            List<ProviderTransactionDTO> transactionsConverted,
                                            String providerName);

    ProviderTransactionDTO toProviderTransactionDTO(Transaction transaction, TransactionStatus transactionStatus);
}
