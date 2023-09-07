package com.yolt.providers.amexgroup.common.mapper;

import com.yolt.providers.amex.common.dto.Account;
import com.yolt.providers.amex.common.dto.Balance;
import com.yolt.providers.amex.common.dto.Transactions;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface AmexGroupDataMapper {
    ProviderAccountDTO mapToAccount(@NotNull Account accountResource,
                                    @Nullable List<Balance> balances,
                                    @Nullable Transactions transactions,
                                    @Nullable Transactions pendingTransactions);
}
