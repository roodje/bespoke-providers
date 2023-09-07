package com.yolt.providers.monorepogroup.raiffeisenatgroup.common.service;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.Account;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.external.Transaction;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.dto.internal.FetchDataResult;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.RaiffeisenAtGroupAccountMapper;
import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.mapper.RaiffeisenAtGroupTransactionMapper;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DefaultRaiffeisenAtGroupDataMappingServiceTest {

    @Mock
    private RaiffeisenAtGroupAccountMapper accountMapper;
    @Mock
    private RaiffeisenAtGroupTransactionMapper transactionMapper;
    @InjectMocks
    private DefaultRaiffeisenAtGroupDataMappingService mappingService;

    @Mock
    Account account1;
    @Mock
    Account account2;
    @Mock
    Transaction bookedTransaction1ForAccount1;
    @Mock
    Transaction bookedTransaction2ForAccount1;
    @Mock
    Transaction pendingTransactionForAccount1;
    @Mock
    Transaction bookedTransactionForAccount2;

    FetchDataResult fetchDataResult;

    @BeforeEach
    void setUp() {
        given(account1.getResourceId()).willReturn("1");
        given(account2.getResourceId()).willReturn("2");
        fetchDataResult = new FetchDataResult();
        fetchDataResult.addResources(account1, List.of(bookedTransaction1ForAccount1, bookedTransaction2ForAccount1), List.of(pendingTransactionForAccount1));
        fetchDataResult.addResources(account2, List.of(bookedTransactionForAccount2), Collections.emptyList());
    }

    @Test
    void mapToDateProviderResponse() {
        var providerAccountDTOBuilderForAccount1 = mock(ProviderAccountDTO.ProviderAccountDTOBuilder.class);
        given(accountMapper.map(account1)).willReturn(providerAccountDTOBuilderForAccount1);
        var mappedBookedTransaction1ForAccount1 = mock(ProviderTransactionDTO.class);
        var mappedBookedTransaction2ForAccount1 = mock(ProviderTransactionDTO.class);
        var mappedPendingTransactionForAccount1 = mock(ProviderTransactionDTO.class);
        given(transactionMapper.map(bookedTransaction1ForAccount1, TransactionStatus.BOOKED))
                .willReturn(mappedBookedTransaction1ForAccount1);
        given(transactionMapper.map(bookedTransaction2ForAccount1, TransactionStatus.BOOKED))
                .willReturn(mappedBookedTransaction2ForAccount1);
        given(transactionMapper.map(pendingTransactionForAccount1, TransactionStatus.PENDING))
                .willReturn(mappedPendingTransactionForAccount1);
        given(providerAccountDTOBuilderForAccount1.transactions(List.of(mappedBookedTransaction1ForAccount1, mappedBookedTransaction2ForAccount1, mappedPendingTransactionForAccount1)))
                .willReturn(providerAccountDTOBuilderForAccount1);
        var mappedAccount1 = mock(ProviderAccountDTO.class);
        given(providerAccountDTOBuilderForAccount1.build())
                .willReturn(mappedAccount1);
        var providerAccountDtoBuilderForAccount2 = mock(ProviderAccountDTO.ProviderAccountDTOBuilder.class);
        given(accountMapper.map(account2)).willReturn(providerAccountDtoBuilderForAccount2);
        var mappedBookedTransactionForAccount2 = mock(ProviderTransactionDTO.class);
        given(transactionMapper.map(bookedTransactionForAccount2, TransactionStatus.BOOKED))
                .willReturn(mappedBookedTransactionForAccount2);
        given(providerAccountDtoBuilderForAccount2.transactions(List.of(mappedBookedTransactionForAccount2)))
                .willReturn(providerAccountDtoBuilderForAccount2);
        var mappedAccount2 = mock(ProviderAccountDTO.class);
        given(providerAccountDtoBuilderForAccount2.build())
                .willReturn(mappedAccount2);
        var expectedMappedResult = new DataProviderResponse(List.of(mappedAccount1, mappedAccount2));

        //when
        var result = mappingService.mapToDateProviderResponse(fetchDataResult);

        //then
        assertThat(result).isEqualTo(expectedMappedResult);


    }
}