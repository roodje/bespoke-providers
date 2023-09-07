package com.yolt.providers.fineco.data.mappers.account;

import com.yolt.providers.fineco.v2.dto.Balance;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.util.List;

public interface FinecoAccountMapper<T, U, V> {
    List<ProviderTransactionDTO> getTransactionList(List<U> accountsTransactionResponse);

    List<Balance> getBalanceList(V accountBalanceResponse);

    ProviderAccountDTO getAccount(T accountDetails,
                                  List<ProviderTransactionDTO> transactions,
                                  List<Balance> balances,
                                  String providerName);

    ProviderAccountNumberDTO getProviderAccountNumberDTO(T accountDetails);
}
