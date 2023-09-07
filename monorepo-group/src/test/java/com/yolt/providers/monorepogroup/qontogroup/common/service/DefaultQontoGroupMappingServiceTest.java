package com.yolt.providers.monorepogroup.qontogroup.common.service;

import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Account;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Transaction;
import com.yolt.providers.monorepogroup.qontogroup.common.dto.internal.QontoFetchDataResult;
import com.yolt.providers.monorepogroup.qontogroup.common.mapper.QontoGroupAccountMapper;
import com.yolt.providers.monorepogroup.qontogroup.common.mapper.QontoGroupTransactionMapper;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DefaultQontoGroupMappingServiceTest {

    @Mock
    QontoGroupAccountMapper accountMapper;
    @Mock
    QontoGroupTransactionMapper transactionMapper;
    @InjectMocks
    DefaultQontoGroupMappingService mappingService;

    @Test
    void shouldReturnMappedData() {
        //given
        var account1 = mock(Account.class);
        var account2 = mock(Account.class);
        var transaction1ForAccount1 = mock(Transaction.class);
        var transaction2ForAccount1 = mock(Transaction.class);
        var transaction1ForAccount2 = mock(Transaction.class);
        var fetchDataResult = new QontoFetchDataResult();
        fetchDataResult.addResources(account1, List.of(transaction1ForAccount1, transaction2ForAccount1));
        fetchDataResult.addResources(account2, List.of(transaction1ForAccount2));

        var providerTransactionDtoForTransaction1ForAccount1 = mock(ProviderTransactionDTO.class);
        given(transactionMapper.map(transaction1ForAccount1)).willReturn(providerTransactionDtoForTransaction1ForAccount1);
        var providerTransactionDtoForTransaction2ForAccount1 = mock(ProviderTransactionDTO.class);
        given(transactionMapper.map(transaction2ForAccount1)).willReturn(providerTransactionDtoForTransaction2ForAccount1);
        var providerTransactionDtoForTransaction1ForAccount2 = mock(ProviderTransactionDTO.class);
        given(transactionMapper.map(transaction1ForAccount2)).willReturn(providerTransactionDtoForTransaction1ForAccount2);

        var providerAccountDtoBuilderForAccount1 = mock(ProviderAccountDTO.ProviderAccountDTOBuilder.class);
        var providerAccountDtoBuilderForAccount2 = mock(ProviderAccountDTO.ProviderAccountDTOBuilder.class);
        given(accountMapper.map(account1)).willReturn(providerAccountDtoBuilderForAccount1);
        given(accountMapper.map(account2)).willReturn(providerAccountDtoBuilderForAccount2);
        given(providerAccountDtoBuilderForAccount1.transactions(List.of(providerTransactionDtoForTransaction1ForAccount1, providerTransactionDtoForTransaction2ForAccount1))).willReturn(providerAccountDtoBuilderForAccount1);
        var providerAccountDtoForAccount1 = mock(ProviderAccountDTO.class);
        given(providerAccountDtoBuilderForAccount1.build()).willReturn(providerAccountDtoForAccount1);

        given(providerAccountDtoBuilderForAccount2.transactions(List.of(providerTransactionDtoForTransaction1ForAccount2))).willReturn(providerAccountDtoBuilderForAccount2);
        var providerAccountDtoForAccount2 = mock(ProviderAccountDTO.class);
        given(providerAccountDtoBuilderForAccount2.build()).willReturn(providerAccountDtoForAccount2);

        //when
        var result = mappingService.mapToListOfProviderAccountDto(fetchDataResult);

        //then
        assertThat(result).containsExactlyInAnyOrderElementsOf(List.of(providerAccountDtoForAccount1, providerAccountDtoForAccount2));

    }
}