package com.yolt.providers.monorepogroup.qontogroup.common.filter;

import com.yolt.providers.monorepogroup.qontogroup.common.dto.external.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DefaultQontoGroupTransactionFilterTest {

    DefaultQontoGroupTransactionFilter transactionsFilter = new DefaultQontoGroupTransactionFilter();

    @Test
    void shouldReturnFilteredTransactionsList() {
        //given
        var completedTransaction = mock(Transaction.class);
        given(completedTransaction.getStatus()).willReturn("completed");
        var pendingTransaction = mock(Transaction.class);
        given(pendingTransaction.getStatus()).willReturn("pending");
        var rejectedTransaction = mock(Transaction.class);
        given(rejectedTransaction.getStatus()).willReturn("rejected");

        //when
        var result = transactionsFilter.apply(List.of(completedTransaction, pendingTransaction, rejectedTransaction));

        //then
        assertThat(result).containsExactlyInAnyOrderElementsOf(List.of(
                completedTransaction, pendingTransaction
        ));
    }

    @Test
    void shouldReturnAllTransactions() {
        //given
        var completedTransaction = mock(Transaction.class);
        given(completedTransaction.getStatus()).willReturn("completed");
        var pendingTransaction = mock(Transaction.class);
        given(pendingTransaction.getStatus()).willReturn("pending");

        //when
        var result = transactionsFilter.apply(List.of(completedTransaction, pendingTransaction));

        //then
        assertThat(result).containsExactlyInAnyOrderElementsOf(List.of(
                completedTransaction, pendingTransaction
        ));
    }


}