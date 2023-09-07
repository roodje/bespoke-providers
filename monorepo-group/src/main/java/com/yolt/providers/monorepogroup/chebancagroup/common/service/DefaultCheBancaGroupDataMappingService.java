package com.yolt.providers.monorepogroup.chebancagroup.common.service;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.external.Account;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.external.Balances;
import com.yolt.providers.monorepogroup.chebancagroup.common.dto.internal.FetchDataResult;
import com.yolt.providers.monorepogroup.chebancagroup.common.mapper.CheBancaGroupAccountMapper;
import com.yolt.providers.monorepogroup.chebancagroup.common.mapper.CheBancaGroupTransactionMapper;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class DefaultCheBancaGroupDataMappingService implements CheBancaGroupDataMappingService {

    private final CheBancaGroupAccountMapper accountMapper;
    private final CheBancaGroupTransactionMapper transactionMapper;

    @Override
    public DataProviderResponse mapToDateProviderResponse(final FetchDataResult fetchDataResult) throws ProviderFetchDataException {
        List<ProviderAccountDTO> mappedAccountsList = new ArrayList<>();
        List<Account> accountList = fetchDataResult.getAccounts();

        for (Account account : accountList) {
            Balances balance = fetchDataResult.getBalances(account.getAccountId());
            ProviderAccountDTO.ProviderAccountDTOBuilder mappedAccountBuilder = accountMapper.map(account, balance);
            Stream<ProviderTransactionDTO> mappedBookedTransactions = fetchDataResult.getBookedTransactions(account.getAccountId()).stream()
                    .map(trx -> transactionMapper.map(trx, TransactionStatus.BOOKED));
            Stream<ProviderTransactionDTO> mappedPendingTransactions = fetchDataResult.getPendingTransactions(account.getAccountId()).stream()
                    .map(trx -> transactionMapper.map(trx, TransactionStatus.PENDING));

            ProviderAccountDTO mappedAccount = mappedAccountBuilder
                    .transactions(Stream.concat(mappedBookedTransactions, mappedPendingTransactions).collect(Collectors.toList()))
                    .build();
            mappedAccountsList.add(mappedAccount);
        }
        return new DataProviderResponse(mappedAccountsList);
    }
}
