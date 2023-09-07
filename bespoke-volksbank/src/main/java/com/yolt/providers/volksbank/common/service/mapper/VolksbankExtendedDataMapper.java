package com.yolt.providers.volksbank.common.service.mapper;

import com.yolt.providers.volksbank.dto.v1_1.AccountDetails;
import com.yolt.providers.volksbank.dto.v1_1.BalanceItem;
import com.yolt.providers.volksbank.dto.v1_1.TransactionItem;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.transaction.ExtendedTransactionDTO;

import java.math.BigDecimal;

public interface VolksbankExtendedDataMapper {

    ExtendedAccountDTO createExtendedAccountDTO(final AccountDetails account,
                                                final BalanceItem balance);

    ExtendedTransactionDTO createExtendedTransactionDTO(final TransactionItem transaction, BigDecimal amount);
}
