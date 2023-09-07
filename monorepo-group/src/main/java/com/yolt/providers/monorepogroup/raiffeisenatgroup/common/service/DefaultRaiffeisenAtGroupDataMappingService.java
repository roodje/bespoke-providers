package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.service;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.Account;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.internal.FetchDataResult;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.RaiffeisenAtGroupAccountMapper;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.RaiffeisenAtGroupTransactionMapper;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class DefaultRaiffeisenAtGroupDataMappingService implements RaiffeisenAtGroupDataMappingService {

    private final RaiffeisenAtGroupAccountMapper accountMapper;
    private final RaiffeisenAtGroupTransactionMapper transactionMapper;

    @Override
    public DataProviderResponse mapToDateProviderResponse(FetchDataResult fetchDataResult) {
        List<ProviderAccountDTO> mappedAccountsList = new ArrayList<>();
        List<Account> accountList = fetchDataResult.getAccounts();
        for (Account account : accountList) {
            ProviderAccountDTO.ProviderAccountDTOBuilder mappedAccountBuilder = accountMapper.map(account);
            Stream<ProviderTransactionDTO> mappedBookedTransactions = fetchDataResult.getBookedTransactions(account.getResourceId()).stream()
                    .map(trx -> transactionMapper.map(trx, TransactionStatus.BOOKED));
            Stream<ProviderTransactionDTO> mappedPendingTransactions = fetchDataResult.getPendingTransactions(account.getResourceId()).stream()
                    .map(trx -> transactionMapper.map(trx, TransactionStatus.PENDING));
            ProviderAccountDTO mappedAccount = mappedAccountBuilder
                    .transactions(Stream.concat(mappedBookedTransactions, mappedPendingTransactions).collect(Collectors.toList()))
                    .build();
            mappedAccountsList.add(mappedAccount);
        }
        return new DataProviderResponse(mappedAccountsList);
    }
}
