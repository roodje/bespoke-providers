package com.yolt.providers.fineco.data;

import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.fineco.data.mappers.account.FinecoAccountMapper;
import com.yolt.providers.fineco.dto.FinecoAccount;
import com.yolt.providers.fineco.v2.dto.Balance;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.util.List;

public class FinecoDataMapperV2 {
    public <T, U, V> void mapRawToUnifiedAccount(final List<ProviderAccountDTO> yoltAccounts,
                                                 final List<FinecoAccount<T, U, V>> finecoAccounts,
                                                 final FinecoAccountMapper<T, U, V> finecoAccountMapper,
                                                 final String providerName) throws ProviderFetchDataException {
        final List<ProviderAccountDTO> accounts = yoltAccounts;

        for (FinecoAccount<T, U, V> finecoAccount : finecoAccounts) {
            try {
                ProviderAccountDTO providerAccountDTO = mapToProviderAccountDTO(finecoAccount, finecoAccountMapper, providerName);
                accounts.add(providerAccountDTO);
            } catch (RuntimeException e) {
                throw new ProviderFetchDataException(e);
            }
        }
    }

    private <T, U, V> ProviderAccountDTO mapToProviderAccountDTO(final FinecoAccount<T, U, V> finecoAccount,
                                                                 final FinecoAccountMapper<T, U, V> finecoAccountMapper,
                                                                 final String providerName) {
        List<ProviderTransactionDTO> transactions = finecoAccountMapper.getTransactionList(finecoAccount.getTransactions());
        List<Balance> balances = finecoAccountMapper.getBalanceList(finecoAccount.getBalances());
        return finecoAccountMapper.getAccount(finecoAccount.getAccount(), transactions, balances, providerName);
    }
}
