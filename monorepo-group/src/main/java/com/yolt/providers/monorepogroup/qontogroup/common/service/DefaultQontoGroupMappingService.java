package com.yolt.providers.monorepogroup.qontogroup.common.service;

import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Transaction;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.internal.QontoFetchDataResult;
import com.yolt.providers.monorepogroup.qontogroup.common.mapper.QontoGroupAccountMapper;
import com.yolt.providers.monorepogroup.qontogroup.common.mapper.QontoGroupTransactionMapper;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class DefaultQontoGroupMappingService implements QontoGroupMappingService {

    private final QontoGroupAccountMapper accountMapper;
    private final QontoGroupTransactionMapper transactionMapper;

    @Override
    public List<ProviderAccountDTO> mapToListOfProviderAccountDto(QontoFetchDataResult fetchDataResult) {
        List<ProviderAccountDTO> mappedAccountList = new ArrayList<>();
        fetchDataResult.getResources().forEach((account, transactionList) -> {
            var providerAccountDtoBuilder = accountMapper.map(account);
            List<ProviderTransactionDTO> mappedTransactionList = new ArrayList<>();
            for (Transaction transaction : transactionList)
                mappedTransactionList.add(transactionMapper.map(transaction));
            providerAccountDtoBuilder.transactions(mappedTransactionList);
            mappedAccountList.add(providerAccountDtoBuilder.build());
        });
        return mappedAccountList;
    }
}
