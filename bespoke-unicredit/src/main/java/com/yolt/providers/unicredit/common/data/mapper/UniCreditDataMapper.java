package com.yolt.providers.unicredit.common.data.mapper;

import com.yolt.providers.unicredit.common.dto.UniCreditAccountDTO;
import com.yolt.providers.unicredit.common.dto.UniCreditBalanceDTO;
import com.yolt.providers.unicredit.common.dto.UniCreditTransactionsDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;

import java.util.List;

public interface UniCreditDataMapper {
    ProviderAccountDTO mapToAccount(final UniCreditAccountDTO account,
                                    final List<UniCreditTransactionsDTO> transactions,
                                    final List<UniCreditBalanceDTO> balances);
    boolean verifyAccountType(String cashAccountType);
}
